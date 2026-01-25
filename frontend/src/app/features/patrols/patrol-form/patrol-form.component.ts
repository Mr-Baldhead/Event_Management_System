import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PatrolService } from '../../../core/services/patrol.service';
import { EventService } from '../../../core/services/event.service';
import { Event } from '../../../core/models/event.model';

@Component({
  selector: 'app-patrol-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatSnackBarModule],
  template: `
    <div class="page-header">
      <button mat-icon-button routerLink="/patrols"><mat-icon>arrow_back</mat-icon></button>
      <h1>{{ isEditMode() ? 'Redigera patrull' : 'Ny patrull' }}</h1>
    </div>
    <mat-card>
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form-container">
          <mat-form-field appearance="outline">
            <mat-label>Namn</mat-label>
            <input matInput formControlName="name">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Beskrivning</mat-label>
            <textarea matInput formControlName="description" rows="3"></textarea>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Evenemang</mat-label>
            <mat-select formControlName="eventId">
              <mat-option [value]="null">Inget evenemang</mat-option>
              @for (e of events(); track e.id) {
                <mat-option [value]="e.id">{{ e.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <div class="form-actions">
            <button mat-button type="button" routerLink="/patrols">Avbryt</button>
            <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">Spara</button>
          </div>
        </form>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`.page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; } h1 { margin: 0; } .form-container { display: flex; flex-direction: column; gap: 8px; max-width: 600px; } .form-actions { display: flex; justify-content: flex-end; gap: 16px; margin-top: 24px; }`]
})
export class PatrolFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly patrolService = inject(PatrolService);
  private readonly eventService = inject(EventService);
  private readonly snackBar = inject(MatSnackBar);

  form!: FormGroup;
  isEditMode = signal(false);
  events = signal<Event[]>([]);
  patrolId: number | null = null;

  ngOnInit(): void {
    this.form = this.fb.group({ name: ['', Validators.required], description: [''], eventId: [null] });
    this.eventService.getAll().subscribe(e => this.events.set(e));
    const id = this.route.snapshot.params['id'];
    if (id) { this.patrolId = +id; this.isEditMode.set(true); this.patrolService.getById(this.patrolId).subscribe(p => this.form.patchValue(p)); }
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const req = this.isEditMode() ? this.patrolService.update(this.patrolId!, this.form.value) : this.patrolService.create(this.form.value);
    req.subscribe({ next: () => { this.snackBar.open('Sparad!', 'Stäng', { duration: 3000 }); this.router.navigate(['/patrols']); }, error: () => this.snackBar.open('Fel', 'Stäng', { duration: 3000 }) });
  }
}
