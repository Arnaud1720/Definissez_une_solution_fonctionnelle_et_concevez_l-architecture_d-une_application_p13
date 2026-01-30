// User models
export type UserRole = 'USER' | 'EMPLOYEE' | 'ADMIN';

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  address?: string;
  role?: UserRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  address?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

// Agency models
export interface Agency {
  id: number;
  name: string;
  address: string;
  city: string;
  country: string;
  postalCode?: string;
  phone?: string;
  email?: string;
}

export interface CityOption {
  city: string;
  country: string;
  label: string;
}

// Offer models
export interface Offer {
  id: number;
  catCar: string;
  startDate: string;
  endDate: string;
  price: number;
  departureAgency: Agency;
  returnAgency: Agency;
}

export interface SearchOfferRequest {
  departureCity: string;
  returnCity: string;
  startDate: string;
  endDate: string;
  catCar?: string;
}

// Rental models
export interface Rental {
  id: number;
  catCar: string;
  startDate: string;
  endDate: string;
  price: number;
  status: RentalStatus;
  departureAgency: Agency;
  returnAgency: Agency;
  refundPercentage?: number;
}

export type RentalStatus = 'BOOKED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface CreateRentalRequest {
  catCar: string;
  startDate: string;
  endDate: string;
  price: number;
  departureAgencyId: number;
  returnAgencyId: number;
  userId?: number;
}

// Vehicle categories
export const VEHICLE_CATEGORIES = [
  { code: 'A', name: 'Économique', emoji: '🚗', color: 'blue' },
  { code: 'B', name: 'Compacte', emoji: '🚙', color: 'cyan' },
  { code: 'C', name: 'Berline', emoji: '🚘', color: 'indigo' },
  { code: 'D', name: 'SUV', emoji: '🚕', color: 'teal' },
  { code: 'E', name: 'Premium', emoji: '🏎️', color: 'amber' }
] as const;

// Payment models
export interface CheckoutSessionResponse {
  url: string;
  sessionId: string;
}

export interface CreateCheckoutSessionRequest {
  catCar: string;
  startDate: string;
  endDate: string;
  price: number;
  departureAgencyId: number;
  returnAgencyId: number;
  departureAgencyName: string;
  returnAgencyName: string;
  departureCity: string;
  returnCity: string;
}

// Conversation / Messaging models
export type ConversationStatus = 'OPEN' | 'CLOSED' | 'PENDING';

export interface Conversation {
  id: number;
  subject: string;
  customerId: number;
  customerName: string;
  employeeId?: number;
  employeeName?: string;
  status: ConversationStatus;
  createdAt: string;
  updatedAt: string;
  unreadCount: number;
  messages?: Message[];
}

export interface Message {
  id: number;
  conversationId: number;
  senderId: number;
  senderName: string;
  content: string;
  sentAt: string;
  isRead: boolean;
}

export interface CreateConversationRequest {
  subject: string;
}

export interface SendMessageRequest {
  conversationId: number;
  content: string;
}
