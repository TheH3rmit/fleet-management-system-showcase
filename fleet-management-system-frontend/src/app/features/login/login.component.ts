import { Component, inject, OnInit, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth/auth.service';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,

    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  loading = false;
  error: string | null = null;
  hidePassword = true;

  form = this.fb.group({
    login: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  ngOnInit() {
    if (this.auth.isAuthenticated()) {
      this.router.navigate(['/menu']);
    }
    this.form.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => (this.error = null));
  }

  onSubmit() {
    if (this.form.invalid || this.loading) return;

    this.loading = true;
    this.error = null;

    const { login, password } = this.form.value;
    this.auth.login(login!, password!).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigateByUrl('/menu');
      },
      error: (e: any) => {
        this.loading = false;
        this.error = e?.userMessage ?? e?.error?.message ?? 'Invalid login or password';
      }
    });
  }
}



