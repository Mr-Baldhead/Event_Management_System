// Field types available in the form builder
export type FieldType = 'TEXT' | 'TEXTAREA' | 'SELECT' | 'CHECKBOX' | 'DATE' | 'EMAIL' | 'PHONE' | 'NUMBER';

// Predefined field types for Swedish scout forms
export type PredefinedType =
    | 'FIRST_NAME'
    | 'LAST_NAME'
    | 'GUARDIAN'        // Field group: Förnamn, Efternamn, Telefon, E-post (50% each)
    | 'ADDRESS'         // Field group: Gatuadress (100%), Postnummer + Ort (50% each)
    | 'PERSONAL_NUMBER'
    | 'PHONE'
    | 'EMAIL'           // Double input for verification
    | 'FOOD_ALLERGY'    // Checkbox list with accordion
    | 'TROOP'           // Dropdown from global list
    | 'OTHER_INFO';     // Textarea

// Field option for select, checkbox, radio fields
export interface FieldOption {
    id?: number;
    value: string;
    label: string;
    sortOrder?: number;
}

// Form field model
export interface FormField {
    id?: number;
    tempId?: string;  // Temporary ID for new fields before save
    eventId?: number;
    label: string;
    fieldType: FieldType;
    sortOrder?: number;
    required?: boolean;
    visible?: boolean;
    placeholder?: string;
    maxLength?: number;
    predefinedType?: PredefinedType;
    isPredefined?: boolean;
    parentFieldId?: number;
    triggerValue?: string;
    rowIndex?: number;
    colPosition?: number;
    colWidth?: number;  // Percentage: 100, 50, or 33
    options?: FieldOption[];
    isFieldGroup?: boolean;  // True for Guardian, Address
    groupFields?: FormField[];  // Child fields in a field group
}

// Row in the form builder canvas
export interface FormRow {
    id: string;
    fields: FormField[];
}

// Troop model (global)
export interface Troop {
    id?: number;
    name: string;
    sortOrder?: number;
}

// Food allergy model (global)
export interface FoodAllergy {
    id?: number;
    name: string;
    sortOrder?: number;
}

// Predefined field template
export interface PredefinedFieldTemplate {
    type: PredefinedType;
    label: string;
    icon: string;
    fieldType: FieldType;
    required: boolean;
    placeholder?: string;
    maxLength?: number;
    options?: FieldOption[];
    description: string;
    isFieldGroup?: boolean;
    hasSettings?: boolean;  // Shows gear icon for Matallergi/Kår
}

// Base field template (standard fields)
export interface BaseFieldTemplate {
    type: FieldType;
    label: string;
    icon: string;
    description: string;
}

// Standard field templates
export const BASE_FIELDS: BaseFieldTemplate[] = [
    { type: 'TEXT', label: 'Text Field', icon: 'text_fields', description: 'Single line text input' },
    { type: 'TEXTAREA', label: 'Text Area', icon: 'notes', description: 'Multi-line text input' },
    { type: 'SELECT', label: 'Dropdown', icon: 'arrow_drop_down_circle', description: 'Select from list' },
    { type: 'DATE', label: 'Datepicker', icon: 'calendar_today', description: 'Date selection' },
    { type: 'CHECKBOX', label: 'Checkbox', icon: 'check_box', description: 'Yes/No selection' }
];

// Predefined field templates for Swedish scout organizations
export const PREDEFINED_FIELDS: PredefinedFieldTemplate[] = [
    {
        type: 'FIRST_NAME',
        label: 'Förnamn',
        icon: 'person',
        fieldType: 'TEXT',
        required: true,
        placeholder: 'Ange förnamn',
        maxLength: 50,
        description: 'Deltagarens förnamn'
    },
    {
        type: 'LAST_NAME',
        label: 'Efternamn',
        icon: 'person',
        fieldType: 'TEXT',
        required: true,
        placeholder: 'Ange efternamn',
        maxLength: 50,
        description: 'Deltagarens efternamn'
    },
    {
        type: 'GUARDIAN',
        label: 'Målsman',
        icon: 'supervisor_account',
        fieldType: 'TEXT',
        required: false,
        description: 'Fältgrupp: Förnamn, Efternamn, Telefon, E-post',
        isFieldGroup: true
    },
    {
        type: 'ADDRESS',
        label: 'Adress',
        icon: 'home',
        fieldType: 'TEXT',
        required: false,
        description: 'Fältgrupp: Gatuadress, Postnummer, Ort',
        isFieldGroup: true
    },
    {
        type: 'PERSONAL_NUMBER',
        label: 'Personnummer',
        icon: 'badge',
        fieldType: 'TEXT',
        required: true,
        placeholder: 'ÅÅÅÅMMDD-XXXX',
        maxLength: 13,
        description: 'Svenskt personnummer'
    },
    {
        type: 'PHONE',
        label: 'Mobilnummer',
        icon: 'phone',
        fieldType: 'PHONE',
        required: false,
        placeholder: '07X-XXX XX XX',
        maxLength: 15,
        description: 'Svenskt mobilnummer'
    },
    {
        type: 'EMAIL',
        label: 'E-post',
        icon: 'email',
        fieldType: 'EMAIL',
        required: true,
        placeholder: 'exempel@domän.se',
        description: 'E-post med verifiering (dubbel input)'
    },
    {
        type: 'FOOD_ALLERGY',
        label: 'Matallergi',
        icon: 'restaurant',
        fieldType: 'CHECKBOX',
        required: false,
        description: 'Fråga + checkboxlista',
        hasSettings: true
    },
    {
        type: 'TROOP',
        label: 'Kår',
        icon: 'groups',
        fieldType: 'SELECT',
        required: false,
        description: 'Välj scoutkår',
        hasSettings: true
    },
    {
        type: 'OTHER_INFO',
        label: 'Övrig information',
        icon: 'info',
        fieldType: 'TEXTAREA',
        required: false,
        placeholder: 'Övrig information...',
        description: 'Fritext för övrig information'
    }
];

// Helper: Generate temporary ID
export function generateTempId(): string {
    return 'temp-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
}

// Helper: Generate row ID
export function generateRowId(): string {
    return 'row-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
}

// Helper: Create field from template
export function createFieldFromTemplate(template: PredefinedFieldTemplate | BaseFieldTemplate): FormField {
    if ('predefinedType' in template || ('type' in template && typeof template.type === 'string' && template.type.includes('_'))) {
        // Predefined field template
        const predefined = template as PredefinedFieldTemplate;
        return {
            tempId: generateTempId(),
            label: predefined.label,
            fieldType: predefined.fieldType,
            predefinedType: predefined.type,
            isPredefined: true,
            required: predefined.required,
            placeholder: predefined.placeholder,
            maxLength: predefined.maxLength,
            options: predefined.options ? [...predefined.options] : [],
            visible: true,
            colWidth: 100,
            isFieldGroup: predefined.isFieldGroup
        };
    } else {
        // Base field template
        const base = template as BaseFieldTemplate;
        return {
            tempId: generateTempId(),
            label: base.label,
            fieldType: base.type,
            isPredefined: false,
            required: false,
            visible: true,
            colWidth: 100,
            options: base.type === 'SELECT' ? [{ value: 'option1', label: 'Alternativ 1' }] : []
        };
    }
}

// Helper: Calculate column widths for a row
export function calculateColWidths(fieldCount: number): number {
    if (fieldCount === 1) return 100;
    if (fieldCount === 2) return 50;
    if (fieldCount >= 3) return 33;
    return 100;
}

// Helper: Get field icon
export function getFieldIcon(field: FormField): string {
    if (field.isPredefined && field.predefinedType) {
        const predefined = PREDEFINED_FIELDS.find(p => p.type === field.predefinedType);
        return predefined?.icon || 'text_fields';
    }
    const base = BASE_FIELDS.find(b => b.type === field.fieldType);
    return base?.icon || 'text_fields';
}
