import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { 
  collection, 
  getDocs, 
  query, 
  orderBy, 
  doc, 
  onSnapshot,
  addDoc,
  updateDoc
} from 'firebase/firestore';
import { db } from '../firebase-config';
import { Chat, ChatMessage } from '../interfaces/Chat';
import { AdminService } from './admin.service';
import { Admin } from '../interfaces/admin';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private chatsCollection = collection(db, 'Chats');

  constructor(private adminService: AdminService) {}

  // Lấy tất cả các chat liên quan đến admin hiện tại
  getChatsForCurrentAdmin(): Observable<Chat[]> {
    const q = query(this.chatsCollection, orderBy('timestamp', 'desc'));

    return new Observable<Chat[]>(observer => {
      const unsubscribe = onSnapshot(q, (snapshot) => {
        const chats: Chat[] = [];
        snapshot.forEach(doc => {
          const data = doc.data() as {
            participants: string[];
            lastMessage?: any; // Could be string or ChatMessage object
            sessionId: number;
            timestamp: any;
            customerName?: string;
          };
          
          // Parse lastMessage properly
          let lastMessage: ChatMessage | undefined;
          if (data.lastMessage) {
            if (typeof data.lastMessage === 'string') {
              // If it's stored as string, create ChatMessage object
              lastMessage = {
                senderId: '',
                message: data.lastMessage,
                timestamp: data.timestamp?.toDate() || new Date(),
                isRead: false
              };
            } else if (typeof data.lastMessage === 'object') {
              // If it's already a ChatMessage object
              lastMessage = {
                _id: data.lastMessage._id,
                senderId: data.lastMessage.senderId || '',
                message: data.lastMessage.message || '',
                timestamp: data.lastMessage.timestamp?.toDate() || new Date(),
                isRead: data.lastMessage.isRead || false
              };
            }
          }

          const chat: Chat = {
            _id: doc.id,
            participants: data.participants || [],
            customerName: data.customerName || 'Unknown',
            lastMessage: lastMessage,
            sessionId: data.sessionId || 0,
            messages: []
          };
          
          chats.push(chat);
        });
        observer.next(chats);
      }, (error) => {
        console.error('Error fetching chats:', error);
        observer.error(error);
      });

      return { unsubscribe };
    });
  }

  // Lấy tin nhắn từ sub-collection của một chat cụ thể
  getChatMessages(chatId: string): Observable<ChatMessage[]> {
    const messagesCollection = collection(db, `chats/${chatId}/messages`);
    const q = query(messagesCollection, orderBy('timestamp', 'asc'));

    return new Observable<ChatMessage[]>(observer => {
      const unsubscribe = onSnapshot(q, (snapshot) => {
        const messages: ChatMessage[] = [];
        snapshot.forEach(doc => {
          const data = doc.data() as { 
            senderId: string; 
            message: string; 
            timestamp: any; 
            isRead: boolean; 
          };
          messages.push({
            _id: doc.id,
            senderId: data.senderId || '',
            message: data.message || '',
            timestamp: data.timestamp?.toDate() || new Date(),
            isRead: data.isRead || false
          });
        });
        observer.next(messages);
      }, (error) => {
        console.error('Error fetching messages:', error);
        observer.error(error);
      });

      return { unsubscribe };
    });
  }

  // Gửi tin nhắn từ admin
  sendMessage(chatId: string, message: string): Promise<void> {
    const { admin: currentAdmin } = this.adminService.getCurrentAdmin();
    if (!currentAdmin) {
      return Promise.reject(new Error('No admin logged in'));
    }

    const messagesCollection = collection(db, `chats/${chatId}/messages`);
    const newMessage: ChatMessage = {
      senderId: currentAdmin.employeeid,
      message: message,
      timestamp: new Date(),
      isRead: false
    };

    return addDoc(messagesCollection, newMessage).then((docRef) => {
      console.log('Message sent successfully');
      
      // Cập nhật lastMessage trong chat document - store as ChatMessage object
      const chatRef = doc(db, 'chats', chatId);
      const lastMessageForChat: ChatMessage = {
        _id: docRef.id,
        senderId: currentAdmin.employeeid,
        message: message,
        timestamp: new Date(),
        isRead: false
      };
      
      return updateDoc(chatRef, { 
        lastMessage: lastMessageForChat, 
        timestamp: new Date() 
      });
    }).catch(error => {
      console.error('Error sending message:', error);
      throw error;
    });
  }

  // Đánh dấu tin nhắn là đã đọc
  markMessageAsRead(chatId: string, messageId: string): Promise<void> {
    const messageRef = doc(db, `chats/${chatId}/messages/${messageId}`);
    return updateDoc(messageRef, { isRead: true }).then(() => {
      console.log('Message marked as read');
    }).catch(error => {
      console.error('Error marking message as read:', error);
      throw error;
    });
  }
  
}