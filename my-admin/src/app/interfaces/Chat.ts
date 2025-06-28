export interface ChatMessage {
  _id?: string;
  senderId: string;
  message: string;
  timestamp: Date;
  isRead: boolean;
}

export interface Chat {
  _id?: string;
  participants: string[];
  customerName?: string;
  lastMessage?: ChatMessage; // Thay đổi từ string thành ChatMessage | null
  messages?: ChatMessage[]; // Mảng tin nhắn, không bắt buộc
  sessionId?: number; // Đặt làm optional nếu không phải luôn có
  
}