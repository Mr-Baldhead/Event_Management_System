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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ParticipantService } from '../../../core/services/participant.service';
import { PatrolService } from '../../../core/services/patrol.service';
import { AllergenService } from '../../../core/services/allergen.service';
import { Patrol } from '../../../core/models/patrol.model';
import { Allergen } from '../../../core/models/allergen.model';

@Component({
  selector: 'app-participant-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatDatepickerModule,
    MatNativeDateModule, MatSnackBarModule
  ],
  template: `
    <div class="page-header">
      <button mat-icon-button routerLink="/participants"><mat-icon>arrow_back</mat-icon></button>
      <h1>{{ isEditMode() ? 'Redigera deltagare' : 'Ny deltagare' }}</h1>
    </div>
    <mat-card>
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form-container">
          <div class="form-row">
            <mat-form-field appearance="outline">
              <mat-label>Förnamn</mat-label>
              <input matInput formControlName="firstName">
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Efternamn</mat-label>
              <input matInput formControlName="lastName">
            </mat-form-field>
          </div>
          <mat-form-field appearance="outline">
            <mat-label>E-post</mat-label>
            <input matInput type="email" formControlName="email">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Telefon</mat-label>
            <input matInput formControlName="phone">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Födelsedatum</mat-label>
            <input matInput [matDatepicker]="picker" formControlName="birthDate">
            <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
            <mat-datepicker #picker></mat-datepicker>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Patrull</mat-label>
            <mat-select formControlName="patrolId">
              <mat-option [value]="null">Ingen patrull</mat-option>
              @for (patrol of patrols(); track patrol.id) {
                <mat-option [value]="patrol.id">{{ patrol.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Allergier</mat-label>
            <mat-select formControlName="allergenIds" multiple>
              @for (allergen of allergens(); track allergen.id) {
                <mat-option [value]="allergen.id">{{ allergen.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <div class="form-actions">
            <button mat-button type="button" routerLink="/participants">Avbryt</button>
            <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">
              {{ isEditMode() ? 'Spara' : 'Skapa' }}
            </button>
          </div>
        </form>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
    h1 { margin: 0; color: #29415A; }
    .form-container { display: flex; flex-direction: column; gap: 8px; max-width: 600px; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .form-actions { display: flex; justify-content: flex-end; gap: 16px; margin-top: 24px; }
  `]
})
export class ParticipantFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly participantService = inject(ParticipantService);
  private readonly patrolService = inject(PatrolService);
  private readonly allergenService = inject(AllergenService);
  private readonly snackBar = inject(MatSnackBar);

  form!: FormGroup;
  isEditMode = signal(false);
  patrols = signal<Patrol[]>([]);
  allergens = signal<Allergen[]>([]);
  participantId: number | null = null;

  ngOnInit(): void {
    this.form = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', Validators.email],
      phone: [''],
      birthDate: [null],
      patrolId: [null],
      allergenIds: [[]]
    });

    this.patrolService.getAll().subscribe(p => this.patrols.set(p));
    this.allergenService.getAll().subscribe(a => this.allergens.set(a));

    const id = this.route.snapshot.params['id'];
    if (id) {
      this.participantId = +id;
      this.isEditMode.set(true);
      this.participantService.getById(this.participantId).subscribe(p => {
        this.form.patchValue({ ...p, birthDate: p.birthDate ? new Date(p.birthDate) : null });
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const data = { ...this.form.value, birthDate: this.form.value.birthDate?.toISOString().split('T')[0] };
    const req = this.isEditMode()
      ? this.participantService.update(this.participantId!, data)
      : this.participantService.create(data);

    req.subscribe({
      next: () => { this.snackBar.open('Sparad!', 'Stäng', { duration: 3000 }); this.router.navigate(['/participants']); },
      error: () => this.snackBar.open('Fel vid sparning', 'Stäng', { duration: 3000 })
    });
  }
}
