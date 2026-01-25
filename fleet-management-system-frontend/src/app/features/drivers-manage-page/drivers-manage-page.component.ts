import { Component, signal, inject, OnInit, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { MatSelectModule } from "@angular/material/select";
import { DriverService } from "../../core/services/driver/driver.service";
import { DriverDTO } from "../../core/models/driver.model";
import { DriverStatus } from "../../core/models/driver-status.model";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { NotificationService } from '../../core/services/notification/notification.service';
import { DriverCreateDialogComponent } from "./driver-create-dialog/driver-create-dialog.component";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatSortModule, Sort } from "@angular/material/sort";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { ConfirmDialogService } from "../../shared/confirm-dialog.service";
import { DriverEditDialogComponent } from "./driver-edit-dialog/driver-edit-dialog.component";
import { WorkLogManageComponent } from "../worklog-page/work-log-manage/work-log-manage.component";
import { Page } from "../../core/models/page.model";
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-drivers-manage-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatSortModule,
    MatTooltipModule,
  ],
  templateUrl: './drivers-manage-page.component.html',
  styleUrl: './drivers-manage-page.component.scss'
})
export class DriversManagePageComponent  implements OnInit {
  private api = inject(DriverService);
  private destroyRef = inject(DestroyRef);
  private notify = inject(NotificationService);
  private dialog = inject(MatDialog);
  private confirm = inject(ConfirmDialogService);

  cols = ['userId', 'name', 'email', 'phone', 'license', 'status', 'actions'];

  page = signal<Page<DriverDTO>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 10,
    number: 0,
  });

  searchCtrl = new FormControl<string>('', { nonNullable: true });
  private searchQuery = signal('');

  pageIndex = signal(0);
  pageSize = signal(10);

  statuses: DriverStatus[] = Object.values(DriverStatus);

  loading = false;
  error?: string;
  sortState = signal<Sort | null>(null);

  ngOnInit(): void {
    this.searchQuery.set(this.searchCtrl.value);
    this.searchCtrl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.searchQuery.set(value ?? '');
        this.pageIndex.set(0);
        this.load();
      });

    this.load();
  }

  onSort(e: Sort) {
    this.sortState.set(e.direction ? e : null);
    this.pageIndex.set(0);
    this.load();
  }

  onPage(e: PageEvent) {
    this.pageIndex.set(e.pageIndex);
    this.pageSize.set(e.pageSize);
    this.load();
  }

  private buildSortParams(): string[] | undefined {
    const s = this.sortState();
    if (!s) return undefined;

    const dir = s.direction;
    switch (s.active) {
      case 'name':
        return [`user.lastName,${dir}`, `user.firstName,${dir}`];
      case 'email':
        return [`user.email,${dir}`];
      case 'phone':
        return [`user.phone,${dir}`];
      case 'license':
        return [`driverLicenseNumber,${dir}`];
      case 'status':
        return [`driverStatus,${dir}`];
      case 'userId':
      default:
        return [`userId,${dir}`];
    }
  }

  load(): void {
    this.loading = true;
    this.error = undefined;

    this.api.list({
      q: this.searchQuery(),
      page: this.pageIndex(),
      size: this.pageSize(),
      sort: this.buildSortParams(),
    }).subscribe({
      next: page => {
        this.page.set(page);
        this.loading = false;
      },
      error: () => {
        this.error = 'Nie udalo sie zaladowac kierowcow.';
        this.loading = false;
        this.notify.error(this.error);
      }
    });
  }

  onStatusChange(d: DriverDTO, status: DriverStatus) {
    if (d.driverStatus === status) return;
    this.api.changeStatus(d.userId, status).subscribe({
      next: updated => {
        const updatedPage = {
          ...this.page(),
          content: this.page().content.map(x => x.userId === d.userId ? updated : x)
        };
        this.page.set(updatedPage);
        this.notify.success('Status kierowcy zaktualizowany');
      },
      error: (err: any) => {
        this.notify.error(err?.userMessage ?? err?.error?.error ?? 'Nie udalo sie zmienic statusu');
      }
    });
  }

  delete(d: DriverDTO) {
    if (!this.canDelete(d)) {
      this.notify.error(this.deleteTooltip(d));
      return;
    }
    this.confirm.confirm({
      title: 'Delete driver',
      message: `Usun kierowce ${d.firstName} ${d.lastName}?`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      icon: 'delete_forever',
      confirmColor: 'warn',
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(ok => {
        if (!ok) return;

        this.api.delete(d.userId)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.notify.success('Kierowca usuniety');
              this.load();
            },
            error: (err: any) =>
              this.notify.error(err?.userMessage ?? err?.error?.error ?? 'Nie udalo sie usunac kierowcy'),
          });
      });
  }

  openCreateDialog() {
    const ref = this.dialog.open(DriverCreateDialogComponent, {
      width: '520px',
      panelClass: 'safe-dialog',
    });

    ref.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((created: DriverDTO | null) => {
        if (!created) return;
        this.notify.success('Driver added');
        this.load();
      });
  }

  openEditDialog(d: DriverDTO) {
    const ref = this.dialog.open(DriverEditDialogComponent, {
      width: '520px',
      panelClass: 'safe-dialog',
      data: { driver: d },
    });

    ref.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((updated: DriverDTO | null) => {
        if (!updated) return;
        this.load();
      });
  }

  openWorkLog(d: DriverDTO) {
    this.dialog.open(WorkLogManageComponent, {
      width: '980px',
      panelClass: 'safe-dialog',
      data: { driverId: d.userId },
    });
  }

  canDelete(d: DriverDTO): boolean {
    return !(d.hasTransports || d.hasWorkLogs);
  }

  deleteTooltip(d: DriverDTO): string {
    if (d.hasTransports && d.hasWorkLogs) {
      return 'Cannot delete: driver has transports and work logs.';
    }
    if (d.hasTransports) {
      return 'Cannot delete: driver has transports.';
    }
    if (d.hasWorkLogs) {
      return 'Cannot delete: driver has work logs.';
    }
    return 'Delete driver';
  }
}



