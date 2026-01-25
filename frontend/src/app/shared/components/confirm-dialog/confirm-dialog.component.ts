import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDialogData {
  title: string;
  message: string;
  itemName?: string;
  confirmText?: string;
  cancelText?: string;
  confirmColor?: 'primary' | 'warn';
  icon?: string;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="dialog-container">
      <div class="dialog-icon" [class.warn]="data.confirmColor === 'warn'">
        <mat-icon>{{ data.icon || 'warning' }}</mat-icon>
      </div>

      <h2 mat-dialog-title>{{ data.title }}</h2>

      <mat-dialog-content>
        @if (data.itemName) {
          <p class="item-name">"{{ data.itemName }}"</p>
        }
        <p class="message">{{ data.message }}</p>

        @if (data.confirmColor === 'warn') {
          <div class="warning-box">
            <mat-icon>warning</mat-icon>
            <span>Detta går inte att ångra!</span>
          </div>
        }
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-stroked-button (click)="onCancel()">
          {{ data.cancelText || 'Avbryt' }}
        </button>
        <button 
          mat-flat-button 
          [color]="data.confirmColor || 'primary'"
          (click)="onConfirm()">
          {{ data.confirmText || 'Bekräfta' }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .dialog-container {
      padding: 24px;
      text-align: center;
      min-width: 350px;
    }

    .dialog-icon {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      background: rgba(29, 150, 140, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 24px;
    }

    .dialog-icon mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #1D968C;
    }

    .dialog-icon.warn {
      background: rgba(220, 53, 69, 0.1);
    }

    .dialog-icon.warn mat-icon {
      color: #dc3545;
    }

    h2 {
      margin: 0 0 16px;
      font-size: 24px;
      font-weight: 600;
    }

    .item-name {
      font-size: 18px;
      font-weight: 600;
      color: #1D968C;
      margin: 8px 0 16px;
    }

    .message {
      color: #5F6166;
      line-height: 1.6;
      margin: 0 0 16px;
    }

    .warning-box {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 8px 16px;
      background: rgba(255, 193, 7, 0.15);
      border: 1px solid #ffc107;
      border-radius: 8px;
      color: #856404;
      font-weight: 500;
      font-size: 14px;
      margin-bottom: 16px;
    }

    .warning-box mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    mat-dialog-actions {
      margin-top: 24px;
      padding: 0;
      gap: 8px;
    }
  `]
})
export class ConfirmDialogComponent {
  data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
  private dialogRef = inject(MatDialogRef<ConfirmDialogComponent>);

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
