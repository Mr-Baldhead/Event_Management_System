import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import {
    DragDropModule,
    CdkDragDrop,
    moveItemInArray,
    transferArrayItem
} from '@angular/cdk/drag-drop';

import { HeaderComponent } from '../../../shared/components/header/header.component';
import { FormFieldService } from '../../../core/services/form-field.service';
import { EventService } from '../../../core/services/event.service';
import { TroopService } from '../../../core/services/troop.service';
import { FoodAllergyService } from '../../../core/services/food-allergy.service';
import {
    FormField,
    FormRow,
    BASE_FIELDS,
    PREDEFINED_FIELDS,
    PredefinedFieldTemplate,
    generateRowId,
    generateTempId,
    createFieldFromTemplate,
    calculateColWidths,
    getFieldIcon
} from '../../../shared/models/form-field.model';
import { TroopManagerDialogComponent } from './troop-manager-dialog/troop-manager-dialog.component';
import { AllergyManagerDialogComponent } from './allergy-manager-dialog/allergy-manager-dialog.component';

@Component({
    selector: 'app-form-builder',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatCheckboxModule,
        MatTooltipModule,
        MatSnackBarModule,
        MatProgressSpinnerModule,
        MatDividerModule,
        MatTabsModule,
        MatDialogModule,
        MatButtonToggleModule,
        DragDropModule,
        HeaderComponent
    ],
    templateUrl: './form-builder.component.html',
    styleUrl: './form-builder.component.scss'
})
export class FormBuilderComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private dialog = inject(MatDialog);
    private formFieldService = inject(FormFieldService);
    private eventService = inject(EventService);
    private troopService = inject(TroopService);
    private foodAllergyService = inject(FoodAllergyService);
    private snackBar = inject(MatSnackBar);

    // Event data
    eventId = signal<number | null>(null);
    eventName = signal<string>('');

    // State signals
    loading = signal(true);
    saving = signal(false);
    previewMode = signal(false);

    // Form rows (canvas)
    rows = signal<FormRow[]>([]);

    // Selected field for properties panel
    selectedField = signal<FormField | null>(null);
    selectedRowId = signal<string | null>(null);

    // Field library
    baseFields = BASE_FIELDS;
    predefinedFields = PREDEFINED_FIELDS;

    // Active tab in field library
    activeTab = signal<'predefined' | 'standard'>('predefined');

    // Track changes
    hasChanges = signal(false);

    // Computed: all row IDs for drag-drop connection
    rowIds = computed(() => this.rows().map(r => 'row-' + r.id));

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.eventId.set(parseInt(id, 10));
            this.loadEventAndFields();
            this.loadGlobalData();
        } else {
            this.router.navigate(['/events']);
        }
    }

    private loadEventAndFields(): void {
        const eventId = this.eventId();
        if (!eventId) return;

        this.loading.set(true);

        // Load event details
        this.eventService.getEvent(eventId).subscribe({
            next: (event) => {
                this.eventName.set(event.name);
            },
            error: () => {
                this.snackBar.open('Kunde inte ladda event', 'Stäng', { duration: 5000 });
                this.router.navigate(['/events']);
            }
        });

        // Load existing form fields
        this.formFieldService.getFields(eventId).subscribe({
            next: (fields) => {
                this.buildRowsFromFields(fields);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.addFirstRow();
            }
        });
    }

    private loadGlobalData(): void {
        this.troopService.loadTroops().subscribe();
        this.foodAllergyService.loadAllergies().subscribe();
    }

    private buildRowsFromFields(fields: FormField[]): void {
        if (fields.length === 0) {
            this.addFirstRow();
            return;
        }

        const rowMap = new Map<number, FormField[]>();
        fields.forEach(field => {
            const rowNum = field.rowIndex ?? 0;
            if (!rowMap.has(rowNum)) {
                rowMap.set(rowNum, []);
            }
            rowMap.get(rowNum)!.push(field);
        });

        const rows: FormRow[] = [];
        const sortedRowNums = Array.from(rowMap.keys()).sort((a, b) => a - b);

        sortedRowNums.forEach((rowNum) => {
            const rowFields = rowMap.get(rowNum)!;
            rowFields.sort((a, b) => (a.colPosition ?? 0) - (b.colPosition ?? 0));
            rows.push({
                id: generateRowId(),
                fields: rowFields
            });
        });

        this.rows.set(rows);
    }

    private addFirstRow(): void {
        const newRow: FormRow = {
            id: generateRowId(),
            fields: []
        };
        this.rows.set([newRow]);
    }

    addRow(): void {
        const newRow: FormRow = {
            id: generateRowId(),
            fields: []
        };
        this.rows.update(rows => [...rows, newRow]);
        this.hasChanges.set(true);
    }

    removeRow(rowId: string): void {
        this.rows.update(rows => rows.filter(r => r.id !== rowId));
        if (this.selectedRowId() === rowId) {
            this.selectedField.set(null);
            this.selectedRowId.set(null);
        }
        this.hasChanges.set(true);
    }

    // Expand field group into multiple fields
    private expandFieldGroup(template: PredefinedFieldTemplate): FormField[] {
        const fields: FormField[] = [];

        if (template.type === 'GUARDIAN') {
            fields.push(
                {
                    tempId: generateTempId(),
                    label: 'Målsman förnamn',
                    fieldType: 'TEXT',
                    predefinedType: 'GUARDIAN',
                    isPredefined: true,
                    required: false,
                    placeholder: 'Förnamn',
                    visible: true,
                    colWidth: 50
                },
                {
                    tempId: generateTempId(),
                    label: 'Målsman efternamn',
                    fieldType: 'TEXT',
                    predefinedType: 'GUARDIAN',
                    isPredefined: true,
                    required: false,
                    placeholder: 'Efternamn',
                    visible: true,
                    colWidth: 50
                },
                {
                    tempId: generateTempId(),
                    label: 'Målsman telefon',
                    fieldType: 'PHONE',
                    predefinedType: 'GUARDIAN',
                    isPredefined: true,
                    required: false,
                    placeholder: '07X-XXX XX XX',
                    visible: true,
                    colWidth: 50
                },
                {
                    tempId: generateTempId(),
                    label: 'Målsman e-post',
                    fieldType: 'EMAIL',
                    predefinedType: 'GUARDIAN',
                    isPredefined: true,
                    required: false,
                    placeholder: 'E-postadress',
                    visible: true,
                    colWidth: 50
                }
            );
        } else if (template.type === 'ADDRESS') {
            fields.push(
                {
                    tempId: generateTempId(),
                    label: 'Gatuadress',
                    fieldType: 'TEXT',
                    predefinedType: 'ADDRESS',
                    isPredefined: true,
                    required: false,
                    placeholder: 'Gatuadress',
                    visible: true,
                    colWidth: 100
                },
                {
                    tempId: generateTempId(),
                    label: 'Postnummer',
                    fieldType: 'TEXT',
                    predefinedType: 'ADDRESS',
                    isPredefined: true,
                    required: false,
                    placeholder: 'XXX XX',
                    maxLength: 6,
                    visible: true,
                    colWidth: 50
                },
                {
                    tempId: generateTempId(),
                    label: 'Ort',
                    fieldType: 'TEXT',
                    predefinedType: 'ADDRESS',
                    isPredefined: true,
                    required: false,
                    placeholder: 'Ort',
                    visible: true,
                    colWidth: 50
                }
            );
        } else if (template.type === 'EMAIL') {
            fields.push(
                {
                    tempId: generateTempId(),
                    label: 'E-post',
                    fieldType: 'EMAIL',
                    predefinedType: 'EMAIL',
                    isPredefined: true,
                    required: true,
                    placeholder: 'E-postadress',
                    visible: true,
                    colWidth: 50
                },
                {
                    tempId: generateTempId(),
                    label: 'Bekräfta e-post',
                    fieldType: 'EMAIL',
                    predefinedType: 'EMAIL',
                    isPredefined: true,
                    required: true,
                    placeholder: 'Bekräfta e-postadress',
                    visible: true,
                    colWidth: 50
                }
            );
        }

        return fields;
    }

    private isFieldGroup(template: PredefinedFieldTemplate): boolean {
        return template.type === 'GUARDIAN' || template.type === 'ADDRESS' || template.type === 'EMAIL';
    }

    // Find first empty row or return null
    private findFirstEmptyRow(): FormRow | null {
        return this.rows().find(r => r.fields.length === 0) || null;
    }

    // Handle drop event
    onDropField(event: CdkDragDrop<FormField[]>, targetRowId: string): void {
        const targetRow = this.rows().find(r => r.id === targetRowId);
        if (!targetRow) return;

        if (event.previousContainer === event.container) {
            moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
            this.updateRowWidths(targetRowId);
        } else if (event.previousContainer.id === 'predefined-list' || event.previousContainer.id === 'standard-list') {
            const template = event.item.data as PredefinedFieldTemplate;

            if (this.isFieldGroup(template)) {
                this.addFieldGroupToCanvas(template, targetRowId);
            } else {
                if (targetRow.fields.length >= 3) {
                    this.snackBar.open('Max 3 fält per rad', 'Stäng', { duration: 3000 });
                    return;
                }

                const newField = createFieldFromTemplate(template);
                targetRow.fields.splice(event.currentIndex, 0, newField);
                this.rows.set([...this.rows()]);
                this.updateRowWidths(targetRowId);
                this.selectField(newField, targetRowId);
            }
        } else {
            if (targetRow.fields.length >= 3) {
                this.snackBar.open('Max 3 fält per rad', 'Stäng', { duration: 3000 });
                return;
            }

            transferArrayItem(
                event.previousContainer.data,
                event.container.data,
                event.previousIndex,
                event.currentIndex
            );

            this.updateRowWidths(targetRowId);
            const prevRowId = event.previousContainer.id.replace('row-', '');
            this.updateRowWidths(prevRowId);
        }

        this.hasChanges.set(true);
    }

    // Add field group - uses target row if empty, otherwise creates new rows
    private addFieldGroupToCanvas(template: PredefinedFieldTemplate, targetRowId: string): void {
        const expandedFields = this.expandFieldGroup(template);
        const targetRow = this.rows().find(r => r.id === targetRowId);
        const targetIsEmpty = targetRow && targetRow.fields.length === 0;

        if (template.type === 'GUARDIAN') {
            // 4 fields: 2 per row
            if (targetIsEmpty) {
                // Use target row for first pair
                targetRow!.fields = [expandedFields[0], expandedFields[1]];
                const row2: FormRow = {
                    id: generateRowId(),
                    fields: [expandedFields[2], expandedFields[3]]
                };
                // Insert row2 after target row
                const targetIndex = this.rows().findIndex(r => r.id === targetRowId);
                const newRows = [...this.rows()];
                newRows.splice(targetIndex + 1, 0, row2);
                this.rows.set(newRows);
            } else {
                const row1: FormRow = {
                    id: generateRowId(),
                    fields: [expandedFields[0], expandedFields[1]]
                };
                const row2: FormRow = {
                    id: generateRowId(),
                    fields: [expandedFields[2], expandedFields[3]]
                };
                this.rows.update(rows => [...rows, row1, row2]);
            }
        } else if (template.type === 'ADDRESS') {
            // 3 fields: 1 + 2
            if (targetIsEmpty) {
                targetRow!.fields = [expandedFields[0]];
                const row2: FormRow = {
                    id: generateRowId(),
                    fields: [expandedFields[1], expandedFields[2]]
                };
                const targetIndex = this.rows().findIndex(r => r.id === targetRowId);
                const newRows = [...this.rows()];
                newRows.splice(targetIndex + 1, 0, row2);
                this.rows.set(newRows);
            } else {
                const row1: FormRow = {
                    id: generateRowId(),
                    fields: [expandedFields[0]]
                };
                const row2: FormRow = {
                    id: generateRowId(),
                    fields: [expandedFields[1], expandedFields[2]]
                };
                this.rows.update(rows => [...rows, row1, row2]);
            }
        } else if (template.type === 'EMAIL') {
            // 2 fields on same row
            if (targetIsEmpty) {
                targetRow!.fields = [expandedFields[0], expandedFields[1]];
                this.rows.set([...this.rows()]);
            } else {
                const row: FormRow = {
                    id: generateRowId(),
                    fields: [expandedFields[0], expandedFields[1]]
                };
                this.rows.update(rows => [...rows, row]);
            }
        }

        this.hasChanges.set(true);
    }

    private updateRowWidths(rowId: string): void {
        this.rows.update(rows => {
            return rows.map(row => {
                if (row.id === rowId) {
                    const width = calculateColWidths(row.fields.length);
                    return {
                        ...row,
                        fields: row.fields.map((f, i) => ({
                            ...f,
                            colWidth: width,
                            colPosition: i
                        }))
                    };
                }
                return row;
            });
        });
    }

    onDropRow(event: CdkDragDrop<FormRow[]>): void {
        moveItemInArray(this.rows(), event.previousIndex, event.currentIndex);
        this.rows.set([...this.rows()]);
        this.hasChanges.set(true);
    }

    selectField(field: FormField, rowId: string): void {
        this.selectedField.set({ ...field });
        this.selectedRowId.set(rowId);
    }

    clearSelection(): void {
        this.selectedField.set(null);
        this.selectedRowId.set(null);
    }

    updateSelectedField(): void {
        const field = this.selectedField();
        const rowId = this.selectedRowId();
        if (!field || !rowId) return;

        this.rows.update(rows => {
            return rows.map(row => {
                if (row.id === rowId) {
                    return {
                        ...row,
                        fields: row.fields.map(f =>
                            (f.id === field.id || f.tempId === field.tempId) ? { ...field } : f
                        )
                    };
                }
                return row;
            });
        });
        this.hasChanges.set(true);
    }

    onLabelChange(value: string): void {
        const field = this.selectedField();
        if (field) {
            this.selectedField.set({ ...field, label: value });
            this.updateSelectedField();
        }
    }

    onPlaceholderChange(value: string): void {
        const field = this.selectedField();
        if (field) {
            this.selectedField.set({ ...field, placeholder: value });
            this.updateSelectedField();
        }
    }

    onRequiredChange(value: boolean): void {
        const field = this.selectedField();
        if (field) {
            this.selectedField.set({ ...field, required: value });
            this.updateSelectedField();
        }
    }

    deleteField(field: FormField, rowId: string): void {
        this.rows.update(rows => {
            return rows.map(row => {
                if (row.id === rowId) {
                    const newFields = row.fields.filter(f =>
                        f.id !== field.id && f.tempId !== field.tempId
                    );
                    const width = calculateColWidths(newFields.length);
                    return {
                        ...row,
                        fields: newFields.map((f, i) => ({
                            ...f,
                            colWidth: width,
                            colPosition: i
                        }))
                    };
                }
                return row;
            });
        });

        if (this.selectedField()?.id === field.id || this.selectedField()?.tempId === field.tempId) {
            this.clearSelection();
        }
        this.hasChanges.set(true);
    }

    togglePreview(): void {
        this.previewMode.update(v => !v);
        if (this.previewMode()) {
            this.clearSelection();
        }
    }

    openTroopManager(): void {
        this.dialog.open(TroopManagerDialogComponent, {
            width: '700px',
            maxHeight: '80vh'
        });
    }

    openAllergyManager(): void {
        this.dialog.open(AllergyManagerDialogComponent, {
            width: '700px',
            maxHeight: '80vh'
        });
    }

    saveForm(): void {
        const eventId = this.eventId();
        if (!eventId) return;

        this.saving.set(true);

        const fields: FormField[] = [];
        this.rows().forEach((row, rowIndex) => {
            row.fields.forEach((field, colIndex) => {
                fields.push({
                    ...field,
                    rowIndex: rowIndex,
                    colPosition: colIndex,
                    sortOrder: fields.length,
                    id: field.id && field.id > 0 ? field.id : undefined,
                    tempId: undefined
                });
            });
        });

        this.formFieldService.saveAllFields(eventId, fields).subscribe({
            next: (saved) => {
                this.buildRowsFromFields(saved);
                this.saving.set(false);
                this.hasChanges.set(false);
                this.snackBar.open('Formuläret har sparats!', 'Stäng', {
                    duration: 3000,
                    panelClass: 'success-snackbar'
                });
            },
            error: () => {
                this.saving.set(false);
                this.snackBar.open('Kunde inte spara formuläret', 'Stäng', {
                    duration: 5000,
                    panelClass: 'error-snackbar'
                });
            }
        });
    }

    goBack(): void {
        const eventId = this.eventId();
        if (eventId) {
            this.router.navigate(['/events', eventId, 'edit']);
        } else {
            this.router.navigate(['/events']);
        }
    }

    getFieldIcon(field: FormField): string {
        return getFieldIcon(field);
    }

    hasSettings(template: PredefinedFieldTemplate): boolean {
        return template.hasSettings === true;
    }

    openFieldSettings(template: PredefinedFieldTemplate, event: MouseEvent): void {
        event.stopPropagation();
        if (template.type === 'TROOP') {
            this.openTroopManager();
        } else if (template.type === 'FOOD_ALLERGY') {
            this.openAllergyManager();
        }
    }

    trackByRowId(index: number, row: FormRow): string {
        return row.id;
    }

    trackByFieldId(index: number, field: FormField): string {
        return field.id?.toString() || field.tempId || index.toString();
    }
}