import { Component, computed, effect, inject, signal } from '@angular/core';
import { AuthService } from '../../core/services/auth/auth.service';
import { LoginHistoryService } from '../../core/services/login-history/login-history.service';
import { LoginHistoryDTO } from '../../core/models/login-history.model';
import { environment } from '../../../environments/environment';

import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from "@angular/common";

@Component({
  selector: 'app-account-page',
  standalone: true,
  imports: [
    CommonModule,
    MatDividerModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './account-page.component.html',
  styleUrl: './account-page.component.scss'
})
export class AccountPageComponent {
  auth = inject(AuthService);
  loginHistoryApi = inject(LoginHistoryService);

  me = computed(() => this.auth.me());
  loginHistory = signal<LoginHistoryDTO[]>([]);
  loadingHistory = signal(false);
  rolesLabel = computed(() => (this.me()?.roles ?? []).join(', ') || '—');

  constructor() {
    effect(() => {
      if (!this.me()?.authenticated) {
        this.loginHistory.set([]);
        return;
      }
      this.loadLoginHistory();
    }, { allowSignalWrites: true });
  }

  loadLoginHistory() {
    if (environment.useMockAuth) {
      this.loginHistory.set([]);
      return;
    }

    this.loadingHistory.set(true);
    this.loginHistoryApi.getMy().subscribe({
      next: entries => {
        const sorted = [...entries].sort((a, b) => {
          const at = a.loggedAt ? new Date(a.loggedAt).getTime() : 0;
          const bt = b.loggedAt ? new Date(b.loggedAt).getTime() : 0;
          return bt - at;
        });
        this.loginHistory.set(sorted.slice(0, 5));
      },
      error: () => this.loginHistory.set([]),
      complete: () => this.loadingHistory.set(false)
    });
  }

  isSuccess(result: string) {
    return result?.toUpperCase() === 'SUCCESS';
  }

  refresh() {
    this.auth.loadMe().subscribe();
  }
}



