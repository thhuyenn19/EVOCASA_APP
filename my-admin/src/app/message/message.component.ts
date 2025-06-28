import { Component, OnInit, OnDestroy, ChangeDetectorRef, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { ChatService } from '../services/chat.service';
import { CustomerService } from '../services/customer.service';
import { AdminService } from '../services/admin.service';
import { Chat, ChatMessage } from '../interfaces/Chat';
import { Admin } from '../interfaces/admin';
import { ICustomer } from '../interfaces/customer';
import { Subscription, combineLatest, of, forkJoin } from 'rxjs';
import { switchMap, catchError, delay, retry } from 'rxjs/operators';

@Component({
  selector: 'app-message',
  templateUrl: './message.component.html',
  styleUrls: ['./message.component.css'],
  standalone: false
})
export class MessageComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('chatMessagesContainer', { static: false }) private chatMessagesContainer!: ElementRef;
  
  chats: Chat[] = [];
  filteredChats: Chat[] = [];
  selectedChat?: Chat;
  messages: ChatMessage[] = [];
  newMessage: string = '';
  currentAdmin: { admin: Admin | null; currentId: string | null } = { admin: null, currentId: null };
  showInfo: boolean = false;
  selectedCustomer?: ICustomer;
  orders: { trackingId: string }[] = [];
  isLoading: boolean = true;
  isLoadingCustomer: boolean = false;
  
  // Auto-scroll properties
  private shouldScrollToBottom = false;
  private lastMessageCount = 0;
  
  // Cache customer data to avoid repeated API calls
  private customerCache: Map<string, ICustomer> = new Map();
  private subscriptions: Subscription[] = [];

  constructor(
    private chatService: ChatService,
    private adminService: AdminService,
    private customerService: CustomerService,
    private cdr: ChangeDetectorRef
  ) {
    this.initializeAdminData();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  private scrollToBottom(): void {
    try {
      if (this.chatMessagesContainer && this.chatMessagesContainer.nativeElement) {
        const element = this.chatMessagesContainer.nativeElement;
        element.scrollTop = element.scrollHeight;
      }
    } catch (err) {
      console.error('Error scrolling to bottom:', err);
    }
  }

  private triggerScrollToBottom(): void {
    this.shouldScrollToBottom = true;
    this.cdr.detectChanges();
  }


  private initializeAdminData(): void {
    try {
      const adminData = this.adminService.getCurrentAdmin();
      this.currentAdmin = {
        admin: adminData.admin,
        currentId: adminData.currentId
      };
      console.log('👤 Admin initialized in constructor:', this.currentAdmin);
    } catch (error) {
      console.error('❌ Error initializing admin data:', error);
    }
  }

  ngOnInit(): void {
    console.log('🚀 Component initialized');
    console.log('👤 Current admin at init:', this.currentAdmin);
    this.ensureAdminDataLoaded();
  }

  private ensureAdminDataLoaded(): void {
    if (!this.currentAdmin.admin || !this.currentAdmin.currentId) {
      console.log('⚠️ Admin data not found, retrying...');
      this.initializeAdminData();
    }

    const adminSub = this.adminService.currentAdmin$.pipe(
      retry(3),
      catchError(error => {
        console.error('❌ Error in admin subscription:', error);
        return of(this.currentAdmin);
      })
    ).subscribe(current => {
      console.log('👤 Admin updated:', current);
      this.currentAdmin = current;
      this.loadChatsData();
    });
    this.subscriptions.push(adminSub);

    this.loadChatsData();
  }

  private loadChatsData(): void {
    console.log('📊 Loading chats data for admin:', this.currentAdmin);
    
    if (!this.currentAdmin.admin?.employeeid) {
      console.log('⚠️ No admin employee ID, skipping chat load');
      this.isLoading = false;
      return;
    }

    const chatSub = this.chatService.getChatsForCurrentAdmin().pipe(
      delay(100),
      retry(3),
      catchError(error => {
        console.error('❌ Error loading chats:', error);
        this.isLoading = false;
        return of([]);
      })
    ).subscribe(chats => {
      console.log('🧾 Chats loaded:', chats);
      this.chats = chats || [];
      this.filteredChats = [...this.chats];
      
      // Update chat participants và load lastMessage cho mỗi chat
      this.updateChatParticipants();
      
      // Load messages for all chats to get lastMessage
      this.loadLastMessagesForAllChats();
      
      // Show UI immediately
      this.isLoading = false;
      this.cdr.detectChanges();
      
      // Load customer data in background
      if (this.chats.length > 0) {
        setTimeout(() => {
          this.loadCustomerDataSimple();
        }, 100);
      }
      
      // Auto-select first chat if none selected and chats exist
      if (!this.selectedChat && this.chats.length > 0) {
        console.log('🎯 Auto-selecting first chat...');
        setTimeout(() => {
          this.selectChat(this.chats[0]);
        }, 500); // Tăng thời gian delay để đảm bảo lastMessage đã được load
      }
    });
    this.subscriptions.push(chatSub);
  }

  private updateChatParticipants(): void {
    if (!this.currentAdmin.admin?.employeeid) {
      console.log('⚠️ No admin employee ID for updating participants');
      return;
    }

    this.chats.forEach(chat => {
      const customerId = chat.participants.find(p => p !== this.currentAdmin.admin?.employeeid);
      if (customerId) {
        (chat as any).customerId = customerId;
        
        // Initialize lastMessage as undefined - will be loaded separately
        chat.lastMessage = undefined;
      }
    });
  }

  // Load lastMessage cho tất cả các chat
  private loadLastMessagesForAllChats(): void {
    console.log('📨 Loading last messages for all chats...');
    
    this.chats.forEach(chat => {
      if (chat._id) {
        this.chatService.getChatMessages(chat._id).pipe(
          retry(1),
          catchError(error => {
            console.error(`❌ Error loading messages for chat ${chat._id}:`, error);
            return of([]);
          })
        ).subscribe(messages => {
          if (messages && messages.length > 0) {
            const lastMessage = messages[messages.length - 1];
            chat.lastMessage = lastMessage;
            
            // Update in filtered chats as well
            const filteredIndex = this.filteredChats.findIndex(c => c._id === chat._id);
            if (filteredIndex !== -1) {
              this.filteredChats[filteredIndex].lastMessage = lastMessage;
            }
            
            console.log(`✅ Last message loaded for chat ${chat._id}:`, lastMessage.message);
            this.cdr.detectChanges();
          } else {
            console.log(`⚠️ No messages found for chat ${chat._id}`);
            chat.lastMessage = undefined;
          }
        });
      }
    });
  }

  // Simple customer data loading
  private loadCustomerDataSimple(): void {
    const customerIds = this.chats
      .map(chat => (chat as any).customerId)
      .filter(id => id && !this.customerCache.has(id));

    if (customerIds.length === 0) {
      this.updateChatNamesFromCache();
      return;
    }

    console.log('📞 Loading customer data for', customerIds.length, 'customers');

    customerIds.forEach(customerId => {
      this.customerService.getCustomerById(customerId).pipe(
        retry(1),
        catchError(error => {
          console.error(`❌ Failed to load customer ${customerId}:`, error);
          return of(null);
        })
      ).subscribe(customer => {
        if (customer) {
          this.customerCache.set(customerId, customer);
          console.log(`✅ Loaded customer: ${customer.Name}`);
          this.updateChatNamesFromCache();
        }
      });
    });
  }

  private updateChatNamesFromCache(): void {
    let hasChanges = false;
    
    this.chats.forEach(chat => {
      const customerId = (chat as any).customerId;
      if (customerId && this.customerCache.has(customerId)) {
        const customer = this.customerCache.get(customerId);
        if (customer) {
          // Always update customer name and data
          chat.customerName = customer.Name;
          (chat as any).customerData = customer;
          hasChanges = true;
        }
      }
    });
    
    if (hasChanges) {
      // Force update the filtered chats as well
      this.filteredChats = [...this.chats];
      this.cdr.detectChanges();
      console.log('✅ Chat names updated from cache');
    }
  }

   selectChat(chat: Chat): void {
    if (!chat || !chat._id) {
      console.log('⚠️ Invalid chat selected');
      return;
    }

    this.showInfo = false;
    this.selectedChat = chat;
    console.log('📨 Selected chat:', chat);

    const customerId = (chat as any).customerId;
    console.log('👤 Customer ID:', customerId);

    // Set selected customer from cache
    if (customerId) {
      if (this.customerCache.has(customerId)) {
        this.selectedCustomer = this.customerCache.get(customerId);
        console.log('✅ Customer loaded from cache:', this.selectedCustomer?.Name);
      } else if ((chat as any).customerData) {
        this.selectedCustomer = (chat as any).customerData;
        console.log('✅ Customer loaded from chat data:', this.selectedCustomer?.Name);
      } else {
        console.log('📞 Loading customer data for selected chat...');
        this.preloadCustomerData(customerId);
      }
    }

    // Load messages
    const messageSub = this.chatService.getChatMessages(chat._id).pipe(
      retry(2),
      catchError(error => {
        console.error('❌ Error loading messages:', error);
        return of([]);
      })
    ).subscribe(messages => {
      this.messages = messages || [];
      console.log('💬 Messages loaded:', this.messages.length);
      
      if (this.messages.length > 0) {
        this.selectedChat!.lastMessage = this.messages[this.messages.length - 1];
      } else {
        this.selectedChat!.lastMessage = undefined;
      }
      
      this.lastMessageCount = this.messages.length;
      
      // Scroll to bottom after loading messages
      setTimeout(() => {
        this.triggerScrollToBottom();
      }, 100);
      
      this.cdr.detectChanges();
    });
    this.subscriptions.push(messageSub);
  }
  private loadMessagesForSelectedChat(chatId: string): void {
    const messageSub = this.chatService.getChatMessages(chatId).pipe(
      retry(2),
      catchError(error => {
        console.error('❌ Error loading messages:', error);
        return of([]);
      })
    ).subscribe(messages => {
      this.messages = messages || [];
      this.lastMessageCount = this.messages.length;
      console.log('💬 Messages loaded for selected chat:', this.messages.length);
      
      // Update last message in selected chat
      const lastMessage = this.messages.length > 0 ? this.messages[this.messages.length - 1] : undefined;
      
      if (this.selectedChat) {
        this.selectedChat.lastMessage = lastMessage;
        
        // Also update in main chats array
        const chatIndex = this.chats.findIndex(c => c._id === this.selectedChat!._id);
        if (chatIndex !== -1) {
          this.chats[chatIndex].lastMessage = lastMessage;
          
          // Update filtered chats as well
          const filteredIndex = this.filteredChats.findIndex(c => c._id === this.selectedChat!._id);
          if (filteredIndex !== -1) {
            this.filteredChats[filteredIndex].lastMessage = lastMessage;
          }
        }
      }
      
      // Trigger scroll to bottom after loading messages
      this.triggerScrollToBottom();
      this.cdr.detectChanges();
    });
    this.subscriptions.push(messageSub);
  }

  private preloadCustomerData(customerId: string): void {
    // Check cache first
    if (this.customerCache.has(customerId)) {
      this.selectedCustomer = this.customerCache.get(customerId);
      this.updateSelectedChatCustomerName();
      return;
    }

    console.log('📞 Loading customer data for:', customerId);
    this.customerService.getCustomerById(customerId).pipe(
      retry(2),
      catchError(error => {
        console.error('❌ Failed to preload customer info:', error);
        return of(null);
      })
    ).subscribe(customer => {
      if (customer) {
        // Cache the customer data
        this.customerCache.set(customerId, customer);
        this.selectedCustomer = customer;
        this.updateSelectedChatCustomerName();
        console.log('✅ Customer data preloaded:', customer.Name);
        this.cdr.detectChanges();
      }
    });
  }

  private updateSelectedChatCustomerName(): void {
    if (this.selectedChat && this.selectedCustomer) {
      this.selectedChat.customerName = this.selectedCustomer.Name;
      const chatIndex = this.chats.findIndex(c => c._id === this.selectedChat!._id);
      if (chatIndex !== -1) {
        this.chats[chatIndex].customerName = this.selectedCustomer.Name;
        (this.chats[chatIndex] as any).customerData = this.selectedCustomer;
        
        // Also update filtered chats
        const filteredIndex = this.filteredChats.findIndex(c => c._id === this.selectedChat!._id);
        if (filteredIndex !== -1) {
          this.filteredChats[filteredIndex].customerName = this.selectedCustomer.Name;
          (this.filteredChats[filteredIndex] as any).customerData = this.selectedCustomer;
        }
      }
      this.cdr.detectChanges();
    }
  }

  loadCustomerDetailsById(customerId: string): Promise<void> {
    return new Promise((resolve, reject) => {
      console.log('🔍 Loading customer:', customerId);
      this.isLoadingCustomer = true;
      
      // Check cache first
      if (this.customerCache.has(customerId)) {
        this.selectedCustomer = this.customerCache.get(customerId);
        this.updateSelectedChatCustomerName();
        this.isLoadingCustomer = false;
        resolve();
        return;
      }
      
      this.customerService.getCustomerById(customerId).pipe(
        retry(2),
        catchError(error => {
          console.error('❌ Failed to load customer info:', error);
          this.isLoadingCustomer = false;
          reject(error);
          return of(null);
        })
      ).subscribe({
        next: customer => {
          this.isLoadingCustomer = false;
          if (customer) {
            console.log('✅ Loaded customer:', customer);
            // Cache the customer data
            this.customerCache.set(customerId, customer);
            this.selectedCustomer = customer;
            this.updateSelectedChatCustomerName();
            this.cdr.detectChanges();
          }
          resolve();
        },
        error: err => {
          this.isLoadingCustomer = false;
          console.error('❌ Customer load error:', err);
          reject(err);
        }
      });
    });
  }

  sendMessage(): void {
    if (this.selectedChat?._id && this.newMessage.trim() && this.currentAdmin.admin?.employeeid) {
      const messageText = this.newMessage.trim();
      this.newMessage = '';

      this.chatService
        .sendMessage(this.selectedChat._id, messageText)
        .then(() => {
          console.log('✅ Message sent successfully');
          
          // Scroll to bottom after sending message
          setTimeout(() => {
            this.triggerScrollToBottom();
          }, 100);
        })
        .catch(error => {
          console.error('❌ Error sending message:', error);
          // Restore message on error
          this.newMessage = messageText;
        });
    } else {
      console.error('❌ Cannot send message - missing data:', {
        chatId: this.selectedChat?._id,
        message: this.newMessage.trim(),
        adminId: this.currentAdmin.admin?.employeeid
      });
    }
  }
   private onNewMessage(): void {
    // Reload messages khi có tin nhắn mới
    if (this.selectedChat?._id) {
      this.chatService.getChatMessages(this.selectedChat._id).subscribe(messages => {
        const newMessageCount = messages.length;
        
        // Chỉ scroll nếu có tin nhắn mới
        if (newMessageCount > this.lastMessageCount) {
          this.messages = messages;
          this.lastMessageCount = newMessageCount;
          
          // Auto scroll khi có tin nhắn mới
          setTimeout(() => {
            this.triggerScrollToBottom();
          }, 100);
          
          this.cdr.detectChanges();
        }
      });
    }
  }


  endConversation(): void {
    if (this.selectedChat?._id && this.currentAdmin.admin?.employeeid) {
      this.chatService
        .sendMessage(this.selectedChat._id, 'Conversation ended by admin')
        .then(() => {
          this.selectedChat = undefined;
          this.messages = [];
          this.showInfo = false;
          this.selectedCustomer = undefined;
          this.lastMessageCount = 0;
          this.cdr.detectChanges();
        })
        .catch(error => {
          console.error('❌ Error ending conversation:', error);
        });
    }
  }

  refreshData(): void {
    console.log('🔄 Refreshing data...');
    this.isLoading = true;
    this.chats = [];
    this.filteredChats = [];
    this.selectedChat = undefined;
    this.messages = [];
    this.selectedCustomer = undefined;
    this.showInfo = false;
    this.lastMessageCount = 0;
    // Clear customer cache on refresh
    this.customerCache.clear();
    
    this.initializeAdminData();
    
    setTimeout(() => {
      this.loadChatsData();
    }, 100);
  }

  viewProfile(): void {
    if (this.selectedCustomer) {
      alert(`Viewing profile of ${this.selectedCustomer.Name}`);
    }
  }

  viewAllOrders(): void {
    alert('Viewing all orders for ' + (this.selectedCustomer?.Name || 'this customer'));
  }

  viewOrderDetails(trackingId: string): void {
    alert(`Viewing details for order ${trackingId}`);
  }

  filterChats(event: Event): void {
    const input = (event.target as HTMLInputElement).value.toLowerCase();
    this.filteredChats = this.chats.filter(chat => {
      const customerName = this.getCustomerNameForChat(chat).toLowerCase();
      return customerName.includes(input);
    });
  }

  performSearch(): void {
    const searchInput = (document.querySelector('.search-input') as HTMLInputElement)?.value.toLowerCase();
    if (searchInput) {
      this.filteredChats = this.chats.filter(chat => {
        const customerName = this.getCustomerNameForChat(chat).toLowerCase();
        return customerName.includes(searchInput);
      });
    } else {
      this.filteredChats = [...this.chats];
    }
  }

  showCustomerInfo(): void {
    console.log('🔍 Toggle customer info. Current state:', this.showInfo);
    console.log('📨 Selected chat:', this.selectedChat);
    console.log('👤 Selected customer:', this.selectedCustomer);
    
    if (!this.selectedChat) {
      console.log('⚠️ No chat selected');
      return;
    }

    this.showInfo = !this.showInfo;
    console.log('🔄 New showInfo state:', this.showInfo);

    if (this.showInfo && !this.selectedCustomer) {
      const customerId = (this.selectedChat as any).customerId;
      console.log('🔍 Customer ID from chat:', customerId);
      
      if (customerId) {
        console.log('📞 Loading customer details...');
        this.loadCustomerDetailsById(customerId).catch(error => {
          console.error('❌ Failed to load customer details:', error);
          this.showInfo = false;
          this.cdr.detectChanges();
        });
      } else {
        console.log('⚠️ No customer ID found in chat');
        this.showInfo = false;
      }
    }
    
    this.cdr.detectChanges();
  }

  handleAttachment(): void {
    alert('Attachment feature is not implemented yet!');
  }

  // Helper methods for template
  trackByChatId(index: number, chat: Chat): string {
    return chat._id || index.toString();
  }

  trackByMessageId(index: number, message: ChatMessage): string {
    return message._id || index.toString();
  }

  getCustomerAvatarForChat(chat: Chat): string {
    const customerId = (chat as any).customerId;
    
    // Try to get from cache first
    if (customerId && this.customerCache.has(customerId)) {
      const customer = this.customerCache.get(customerId);
      return customer?.Image || '/assets/images/default-avatar.png';
    }
    
    // Fallback to selected customer if it matches
    if (customerId && this.selectedCustomer && this.selectedCustomer._id === customerId) {
      return this.selectedCustomer.Image || '/assets/images/default-avatar.png';
    }
    
    // Check if customer data is stored in chat
    if ((chat as any).customerData) {
      return (chat as any).customerData.Image || '/assets/images/default-avatar.png';
    }
    
    return '/assets/images/default-avatar.png';
  }

  getCustomerNameForChat(chat: Chat): string {
    const customerId = (chat as any).customerId;
    
    // Try to get from cache first (highest priority)
    if (customerId && this.customerCache.has(customerId)) {
      const customer = this.customerCache.get(customerId);
      return customer?.Name || 'Unknown Customer';
    }
    
    // Check if customer data is stored in chat
    if ((chat as any).customerData) {
      return (chat as any).customerData.Name || 'Unknown Customer';
    }
    
    // Check if customerName is already set in chat
    if (chat.customerName && chat.customerName !== 'Unknown Customer') {
      return chat.customerName;
    }
    
    // Fallback to selected customer if it matches
    if (customerId && this.selectedCustomer && this.selectedCustomer._id === customerId) {
      return this.selectedCustomer.Name || 'Unknown Customer';
    }
    
    return 'Unknown Customer';
  }

  isAdminMessage(message: ChatMessage): boolean {
    return message.senderId === this.currentAdmin.admin?.employeeid;
  }

  isCustomerMessage(message: ChatMessage): boolean {
    return message.senderId !== this.currentAdmin.admin?.employeeid;
  }

  getSenderName(message: ChatMessage): string {
    if (this.isAdminMessage(message)) {
      return this.currentAdmin.admin?.FullName || 'Admin';
    } else {
      return this.selectedCustomer?.Name || 'Customer';
    }
  }

  isChatSelected(chat: Chat): boolean {
    return this.selectedChat?._id === chat._id;
  }

  getChatPreviewMessage(chat: Chat): string {
    if (chat.lastMessage?.message) {
      return chat.lastMessage.message.length > 50 
        ? chat.lastMessage.message.substring(0, 50) + '...'
        : chat.lastMessage.message;
    }
    return 'No messages yet';
  }

  formatTimestamp(timestamp: any): string {
    if (!timestamp) return '';
    
    const date = new Date(timestamp);
    const now = new Date();
    const diffInMs = now.getTime() - date.getTime();
    const diffInHours = diffInMs / (1000 * 60 * 60);
    
    if (diffInHours < 24) {
      return date.toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit',
        hour12: false 
      });
    } else {
      return date.toLocaleDateString('en-US', { 
        month: 'short', 
        day: 'numeric' 
      });
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => {
      if (sub && !sub.closed) {
        sub.unsubscribe();
      }
    });
    this.subscriptions = [];
    this.customerCache.clear();
  }
}