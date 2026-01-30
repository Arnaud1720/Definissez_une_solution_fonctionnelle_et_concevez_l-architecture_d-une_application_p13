import { Component, OnInit, signal, computed, ViewChild, ElementRef, AfterViewChecked, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConversationService, AuthService } from '../../core/services';
import { Conversation, Message, UserRole } from '../../core/models';

@Component({
  selector: 'app-messaging',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './messaging.component.html',
  styleUrl: './messaging.component.css'
})
export class MessagingComponent implements OnInit, AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  private conversationService = inject(ConversationService);
  private authService = inject(AuthService);

  newSubject = signal<string>('');
  newMessage = signal<string>('');
  showNewConversationForm = signal<boolean>(false);
  selectedConversationId = signal<number | null>(null);

  private shouldScrollToBottom = false;

  currentUserId = computed(() => this.authService.currentUser()?.id);
  userRole = computed<UserRole | undefined>(() => this.authService.currentUser()?.role);
  isEmployee = computed(() => this.userRole() === 'EMPLOYEE' || this.userRole() === 'ADMIN');

  conversations = this.conversationService.conversations;
  currentConversation = this.conversationService.currentConversation;
  messages = this.conversationService.messages;
  loading = this.conversationService.loading;
  totalUnreadCount = this.conversationService.totalUnreadCount;

  ngOnInit(): void {
    this.loadConversations();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  loadConversations(): void {
    this.conversationService.loadMyConversations().subscribe();
  }

  loadUnassigned(): void {
    this.conversationService.loadUnassignedConversations().subscribe();
  }

  selectConversation(conversation: Conversation): void {
    this.selectedConversationId.set(conversation.id);
    this.conversationService.getConversationById(conversation.id).subscribe();
    this.conversationService.loadMessages(conversation.id).subscribe(() => {
      this.shouldScrollToBottom = true;
      if (conversation.unreadCount > 0) {
        this.conversationService.markAsRead(conversation.id).subscribe();
      }
    });
  }

  createConversation(): void {
    const subject = this.newSubject().trim();
    if (!subject) return;

    this.conversationService.createConversation({ subject }).subscribe({
      next: (response) => {
        this.showNewConversationForm.set(false);
        this.newSubject.set('');
        this.selectConversation(response.conversation);
      }
    });
  }

  sendMessage(): void {
    const content = this.newMessage().trim();
    const conversationId = this.selectedConversationId();
    if (!content || !conversationId) return;

    this.conversationService.sendMessage({ conversationId, content }).subscribe({
      next: () => {
        this.newMessage.set('');
        this.shouldScrollToBottom = true;
      }
    });
  }

  assignToMe(conversationId: number): void {
    this.conversationService.assignConversation(conversationId).subscribe({
      next: () => {
        this.loadConversations();
      }
    });
  }

  closeConversation(conversationId: number): void {
    this.conversationService.closeConversation(conversationId).subscribe();
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  isMyMessage(message: Message): boolean {
    return message.senderId === this.currentUserId();
  }

  private scrollToBottom(): void {
    if (this.messagesContainer) {
      const element = this.messagesContainer.nativeElement;
      element.scrollTop = element.scrollHeight;
    }
  }

  toggleNewConversationForm(): void {
    this.showNewConversationForm.update(v => !v);
  }

  backToList(): void {
    this.conversationService.clearCurrentConversation();
    this.selectedConversationId.set(null);
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'OPEN': return 'Ouvert';
      case 'CLOSED': return 'Ferme';
      case 'PENDING': return 'En attente';
      default: return status;
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'OPEN': return 'status-open';
      case 'CLOSED': return 'status-closed';
      case 'PENDING': return 'status-pending';
      default: return '';
    }
  }
}
