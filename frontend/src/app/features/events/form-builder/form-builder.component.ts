import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
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
import { MatExpansionModule } from '@angular/material/expansion';
import { MatChipsModule } from '@angular/material/chips';
import { DragDropModule, CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';

import { HeaderComponent } from '../../../shared/components/header/header.component';
import { FormFieldService } from '../../../core/services/form-field.service';
import { EventService } from '../../../core/services/event.service';
import {
    FormField,
    FormRow,
    FieldType,
    PredefinedType,
    BASE_FIELDS,
    PREDEFINED_FIELDS,
    BaseFieldTemplate,
    PredefinedFieldTemplate,
    FieldOption
} from '../../../shared/models/form-field.model';

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
        MatExpansionModule,
        MatChipsModule,
        DragDropModule,
        HeaderComponent
    ],
    templateUrl: './form-builder.component.html',
    styleUrl: './form-builder.component.scss'
})
export class FormBuilderComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private formFieldService = inject(FormFieldService);
    private eventService = inject(EventService);
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

    // Computed: check if form has changes
    hasChanges = signal(false);

    // Computed: all row IDs for drag-drop connection
    rowIds = computed(() => this.rows().map(r => 'row-' + r.id));

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.eventId.set(parseInt(id, 10));
            this.loadEventAndFields();
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
            error: (err) => {
                console.error('Failed to load event:', err);
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
            error: (err) => {
                console.error('Failed to load fields:', err);
                this.loading.set(false);
                // Start with empty canvas if no fields exist
                this.rows.set([]);
            }
        });
    }

    private buildRowsFromFields(fields: FormField[]): void {
        if (fields.length === 0) {
            // Start with one empty row instead of empty canvas
            this.rows.set([{
                id: this.generateRowId(),
                fields: []
            }]);
            return;
        }


        // Group fields by row number
        const rowMap = new Map<number, FormField[]>();
        fields.forEach(field => {
            const rowNum = field.rowIndex ?? 0;
            if (!rowMap.has(rowNum)) {
                rowMap.set(rowNum, []);
            }
            rowMap.get(rowNum)!.push(field);
        });

        // Sort each row by col position and create FormRow objects
        const rows: FormRow[] = [];
        const sortedRowNums = Array.from(rowMap.keys()).sort((a, b) => a - b);

        sortedRowNums.forEach((rowNum, index) => {
            const rowFields = rowMap.get(rowNum)!;
            rowFields.sort((a, b) => (a.colPosition ?? 0) - (b.colPosition ?? 0));
            rows.push({
                id: this.generateRowId(),
                fields: rowFields
            });
        });

        this.rows.set(rows);
    }

    private generateRowId(): string {
        return 'row-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
    }

    private generateFieldId(): number {
        // Temporary negative ID for new fields (will be replaced by backend)
        return -Date.now();
    }

    // Add new row
    addRow(): void {
        const newRow: FormRow = {
            id: this.generateRowId(),
            fields: []
        };
        this.rows.update(rows => [...rows, newRow]);
        this.hasChanges.set(true);
    }

    // Remove row
    removeRow(rowId: string): void {
        this.rows.update(rows => rows.filter(r => r.id !== rowId));
        if (this.selectedRowId() === rowId) {
            this.selectedField.set(null);
            this.selectedRowId.set(null);
        }
        this.hasChanges.set(true);
    }

    // Move row up
    moveRowUp(index: number): void {
        if (index <= 0) return;
        this.rows.update(rows => {
            const newRows = [...rows];
            [newRows[index - 1], newRows[index]] = [newRows[index], newRows[index - 1]];
            return newRows;
        });
        this.hasChanges.set(true);
    }

    // Move row down
    moveRowDown(index: number): void {
        const currentRows = this.rows();
        if (index >= currentRows.length - 1) return;
        this.rows.update(rows => {
            const newRows = [...rows];
            [newRows[index], newRows[index + 1]] = [newRows[index + 1], newRows[index]];
            return newRows;
        });
        this.hasChanges.set(true);
    }

    // Handle drop from field library to canvas
    onDropField(event: CdkDragDrop<FormField[]>, targetRowId: string): void {
        if (event.previousContainer === event.container) {
            // Reorder within same row
            moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
        } else if (event.previousContainer.id === 'base-fields-list' || event.previousContainer.id === 'predefined-fields-list') {
            // Drop from library - create new field
            const template = event.item.data;
            const newField = this.createFieldFromTemplate(template);

            // Insert at drop position
            event.container.data.splice(event.currentIndex, 0, newField);

            // Select the new field
            this.selectField(newField, targetRowId);
        } else {
            // Transfer between rows
            transferArrayItem(
                event.previousContainer.data,
                event.container.data,
                event.previousIndex,
                event.currentIndex
            );
        }
        this.hasChanges.set(true);
    }

    private createFieldFromTemplate(template: BaseFieldTemplate | PredefinedFieldTemplate): FormField {
        if ('predefinedType' in template || 'fieldType' in template) {
            // Predefined field template
            const predefined = template as PredefinedFieldTemplate;
            return {
                id: this.generateFieldId(),
                label: predefined.label,
                fieldType: predefined.fieldType,
                predefinedType: predefined.type,
                isPredefined: true,
                required: predefined.required,
                placeholder: predefined.placeholder,
                validationPattern: predefined.validationPattern,
                maxLength: predefined.maxLength,
                options: predefined.options ? [...predefined.options] : [],
                visible: true,
                colWidth: 12
            };
        } else {
            // Base field template
            const base = template as BaseFieldTemplate;
            return {
                id: this.generateFieldId(),
                label: base.label,
                fieldType: base.type,
                isPredefined: false,
                required: false,
                visible: true,
                colWidth: 12,
                options: base.type === 'SELECT' ? [{ value: 'option1', label: 'Alternativ 1' }] : []
            };
        }
    }

    // Select a field for editing
    selectField(field: FormField, rowId: string): void {
        this.selectedField.set({ ...field });
        this.selectedRowId.set(rowId);
    }

    // Clear field selection
    clearSelection(): void {
        this.selectedField.set(null);
        this.selectedRowId.set(null);
    }

    // Update field from properties panel
    updateSelectedField(): void {
        const field = this.selectedField();
        const rowId = this.selectedRowId();
        if (!field || !rowId) return;

        this.rows.update(rows => {
            return rows.map(row => {
                if (row.id === rowId) {
                    return {
                        ...row,
                        fields: row.fields.map(f => f.id === field.id ? { ...field } : f)
                    };
                }
                return row;
            });
        });
        this.hasChanges.set(true);
    }

    // Delete field
    deleteField(field: FormField, rowId: string): void {
        this.rows.update(rows => {
            return rows.map(row => {
                if (row.id === rowId) {
                    return {
                        ...row,
                        fields: row.fields.filter(f => f.id !== field.id)
                    };
                }
                return row;
            });
        });

        if (this.selectedField()?.id === field.id) {
            this.clearSelection();
        }
        this.hasChanges.set(true);
    }

    // Add option to select field
    addOption(): void {
        const field = this.selectedField();
        if (!field) return;

        const options = field.options || [];
        const newOption: FieldOption = {
            value: `option${options.length + 1}`,
            label: `Alternativ ${options.length + 1}`
        };

        this.selectedField.set({
            ...field,
            options: [...options, newOption]
        });
        this.updateSelectedField();
    }

    // Remove option from select field
    removeOption(index: number): void {
        const field = this.selectedField();
        if (!field || !field.options) return;

        const newOptions = field.options.filter((_, i) => i !== index);
        this.selectedField.set({
            ...field,
            options: newOptions
        });
        this.updateSelectedField();
    }

    // Update option
    updateOption(index: number, key: 'value' | 'label', value: string): void {
        const field = this.selectedField();
        if (!field || !field.options) return;

        const newOptions = [...field.options];
        newOptions[index] = { ...newOptions[index], [key]: value };

        this.selectedField.set({
            ...field,
            options: newOptions
        });
        this.updateSelectedField();
    }

    // Toggle preview mode
    togglePreview(): void {
        this.previewMode.update(v => !v);
        if (this.previewMode()) {
            this.clearSelection();
        }
    }

    // Save form
    saveForm(): void {
        const eventId = this.eventId();
        if (!eventId) return;

        this.saving.set(true);

        // Convert rows to flat field list with row/col info
        const fields: FormField[] = [];
        this.rows().forEach((row, rowIndex) => {
            row.fields.forEach((field, colIndex) => {
                fields.push({
                    ...field,
                    rowIndex: rowIndex,
                    colPosition: colIndex,
                    sortOrder: fields.length,
                    // Remove temporary negative IDs for new fields
                    id: field.id && field.id > 0 ? field.id : undefined
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
            error: (err) => {
                console.error('Failed to save form:', err);
                this.saving.set(false);
                this.snackBar.open('Kunde inte spara formuläret', 'Stäng', {
                    duration: 5000,
                    panelClass: 'error-snackbar'
                });
            }
        });
    }

    // Navigate back to event
    goBack(): void {
        const eventId = this.eventId();
        if (eventId) {
            this.router.navigate(['/events', eventId, 'edit']);
        } else {
            this.router.navigate(['/events']);
        }
    }

    // Get field type label
    getFieldTypeLabel(type: FieldType): string {
        const found = this.baseFields.find(f => f.type === type);
        return found?.label || type;
    }

    // Get field icon
    getFieldIcon(field: FormField): string {
        if (field.isPredefined && field.predefinedType) {
            const predefined = this.predefinedFields.find(p => p.type === field.predefinedType);
            return predefined?.icon || 'text_fields';
        }
        const base = this.baseFields.find(b => b.type === field.fieldType);
        return base?.icon || 'text_fields';
    }

    // Check if field type has options
    hasOptions(fieldType: FieldType): boolean {
        return fieldType === 'SELECT';
    }

    // Track function for ngFor
    trackByRowId(index: number, row: FormRow): string {
        return row.id;
    }

    trackByFieldId(index: number, field: FormField): number {
        return field.id || index;
    }
}