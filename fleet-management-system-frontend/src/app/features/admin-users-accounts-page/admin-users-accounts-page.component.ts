import { Component, DestroyRef, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, finalize, forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSortModule, Sort } from '@angular/material/sort';

import { UserService } from '../../core/services/user/user.service';
import { AccountService } from '../../core/services/account/account.service';
import { NotificationService } from '../../core/services/notification/notification.service';

import { Page } from '../../core/models/page.model';
import { UserResponseDTO } from '../../core/models/user.model';
import { AccountResponseDTO } from '../../core/models/account.model';

import { AdminUserAccountDialogComponent } from './admin-user-account-dialog/admin-user-account-dialog.component';
import { AdminAccountManageDialogComponent } from './admin-account-manage-dialog/admin-account-manage-dialog.component';
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { MatTooltip } from "@angular/material/tooltip";
import { ConfirmDialogService } from "../../shared/confirm-dialog.service";

export interface AdminUserAccountRow {
  user: UserResponseDTO;
  account?: AccountResponseDTO | null;
}

@Component({
  selector: 'app-admin-users-accounts-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,

    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatSortModule,
    MatProgressSpinner,
    MatTooltip,
  ],
  templateUrl: './admin-users-accounts-page.component.html',
  styleUrls: ['./admin-users-accounts-page.component.scss'],
})
export class AdminUsersAccountsPageComponent implements OnInit {
  private usersApi = inject(UserService);
  private accountsApi = inject(AccountService);
  private notify = inject(NotificationService);
  private dialog = inject(MatDialog);
  private ar = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  private confirm = inject(ConfirmDialogService);
  cols = ['id', 'name', 'email', 'login', 'status', 'roles', 'actions'];
  searchCtrl = new FormControl<string>('', { nonNullable: true });

  private pageState = signal<Page<UserResponseDTO> | null>(null);
  private accountsMap = signal<Record<number, AccountResponseDTO | null>>({}); // cache by accountId

  // pagination & sort in URL
  pageIndex = signal(Number(this.ar.snapshot.queryParamMap.get('page') ?? 0));
  pageSize = signal(Number(this.ar.snapshot.queryParamMap.get('size') ?? 10));

  page = computed(() => this.pageState());

  rows = computed<AdminUserAccountRow[]>(() => {
    const p = this.pageState();
    const mapAcc = this.accountsMap();
    if (!p) return [];

    const list = p.content.map(u => ({
      user: u,
      account: u.accountId ? (mapAcc[u.accountId] ?? null) : null
    }));

    const s = this.sortState();
    if (!s) return list;

    const dir = s.direction === 'asc' ? 1 : -1;

    const getValue = (r: AdminUserAccountRow) => {
      switch (s.active) {
        case 'id': return r.user.id;
        case 'name': return `${r.user.firstName ?? ''} ${r.user.lastName ?? ''}`.trim();
        case 'email': return r.user.email ?? '';
        case 'login': return r.account?.login ?? '';
        case 'status': return r.account?.status ?? '';
        case 'roles': return (r.account?.roles ?? []).join(', ');
        default: return '';
      }
    };

    return [...list].sort((a, b) => {
      const av = getValue(a) ?? '';
      const bv = getValue(b) ?? '';
      if (av === bv) return 0;
      return av > bv ? dir : -dir;
    });
  });
  loading = false;

  ngOnInit() {
    const q = this.ar.snapshot.queryParamMap.get('q') ?? '';
    this.searchCtrl.setValue(q);

    this.searchCtrl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.pageIndex.set(0);
        this.syncUrl();
        this.reload();
      });

    this.reload();
  }

  private syncUrl() {
    this.router.navigate([], {
      relativeTo: this.ar,
      queryParams: {
        q: this.searchCtrl.value || null,
        page: this.pageIndex(),
        size: this.pageSize()
      },
      queryParamsHandling: 'merge',
    });
  }
  reload() {
    this.loading = true;
    const q = this.searchCtrl.value || undefined;

    this.usersApi.list({
      q,
      page: this.pageIndex(),
      size: this.pageSize(),
    })
      .pipe(
        switchMap(p => {
          this.pageState.set(p);

          const ids: number[] = Array.from(new Set(
            p.content
              .map((u: UserResponseDTO) => u.accountId)
              .filter((x: number | null | undefined): x is number => !!x)
          ));

          if (ids.length === 0) {
            this.accountsMap.set({});
            return of(p);
          }

          return forkJoin(
            ids.map((id: number) =>
              this.accountsApi.get(id).pipe(
                map(acc => [id, acc] as const),
                catchError(() => of([id, null] as const))
              )
            )
          ).pipe(
            map(pairs => {
              const next: Record<number, AccountResponseDTO | null> = {};
              for (const [id, acc] of pairs as Array<[number, AccountResponseDTO | null]>) {
                next[id] = acc;
              }
              this.accountsMap.set(next);
              return p;
            })
          );
        }),
        finalize(() => { this.loading = false; }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        error: (err: any) => this.notify.error(err?.userMessage ?? 'Failed to load users'),
      });
  }

  onPage(e: PageEvent) {
    this.pageIndex.set(e.pageIndex);
    this.pageSize.set(e.pageSize);
    this.syncUrl();
    this.reload();
  }

  sortState = signal<Sort | null>(null);

  onSort(e: Sort) {
    this.sortState.set(e.direction ? e : null);
    this.pageIndex.set(0);

  }


  openCreate() {
    const ref = this.dialog.open(AdminUserAccountDialogComponent, {
      width: '720px',
      panelClass: 'safe-dialog',
      data: { mode: 'create', title: 'Create user + account' }
    });
    ref.afterClosed().subscribe(res => { if (res === 'ok') this.reload(); });
  }

  openEdit(row: { user: UserResponseDTO, account?: AccountResponseDTO | null }) {
    const ref = this.dialog.open(AdminUserAccountDialogComponent, {
      width: '720px',
      panelClass: 'safe-dialog',
      data: { mode: 'edit', title: 'Edit user + account', user: row.user, account: row.account ?? null }
    });
    ref.afterClosed().subscribe(res => { if (res === 'ok') this.reload(); });
  }

  openManageAccount(row: AdminUserAccountRow) {
    if (!row.account) {
      this.notify.warn('User has no linked account');
      return;
    }
    const ref = this.dialog.open(AdminAccountManageDialogComponent, {
      width: '640px',
      panelClass: 'safe-dialog',
      data: { account: row.account, user: row.user }
    });

    ref.afterClosed().subscribe(res => {
      if (res === 'ok') this.reload();
    });
  }

  removeUser(row: AdminUserAccountRow) {
    const u = row.user;

    this.confirm.confirm({
      title: 'Delete user',
      message: `Delete ${u.firstName} ${u.lastName}?`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      icon: 'delete_forever',
      confirmColor: 'warn',
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(ok => {
        if (!ok) return;

        this.usersApi.delete(u.id).subscribe({
          next: () => { this.notify.success('Deleted'); this.reload(); },
          error: (err: any) => this.notify.error(err?.userMessage ?? err?.error?.message ?? 'Delete failed'),
        });
      });
  }

}



