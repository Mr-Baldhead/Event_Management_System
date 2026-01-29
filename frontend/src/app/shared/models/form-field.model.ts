// Field types available in the form builder
export type FieldType = 'TEXT' | 'TEXTAREA' | 'SELECT' | 'CHECKBOX' | 'DATE' | 'EMAIL' | 'PHONE' | 'NUMBER';

// Predefined field types for Swedish scout forms
export type PredefinedType =
    | 'FIRST_NAME'
    | 'LAST_NAME'
    | 'PERSONAL_NUMBER'
    | 'PHONE'
    | 'EMAIL'
    | 'BIRTH_DATE'
    | 'GUARDIAN'
    | 'ALLERGY'
    | 'TROOP';

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
    eventId?: number;
    label: string;
    fieldType: FieldType;
    sortOrder?: number;
    required?: boolean;
    visible?: boolean;
    validationPattern?: string;
    placeholder?: string;
    maxLength?: number;
    predefinedType?: PredefinedType;
    isPredefined?: boolean;
    parentFieldId?: number;
    triggerValue?: string;
    rowIndex?: number;
    colPosition?: number;
    colWidth?: number;
    options?: FieldOption[];
}

// Row in the form builder canvas
export interface FormRow {
    id: string;
    fields: FormField[];
}

// Predefined field template
export interface PredefinedFieldTemplate {
    type: PredefinedType;
    label: string;
    icon: string;
    fieldType: FieldType;
    required: boolean;
    placeholder?: string;
    validationPattern?: string;
    maxLength?: number;
    options?: FieldOption[];
    description: string;
}

// Base field template
export interface BaseFieldTemplate {
    type: FieldType;
    label: string;
    icon: string;
    description: string;
}

// Field library containing all available fields
export const BASE_FIELDS: BaseFieldTemplate[] = [
    { type: 'TEXT', label: 'Textfält', icon: 'text_fields', description: 'Enrads textinmatning' },
    { type: 'TEXTAREA', label: 'Textarea', icon: 'notes', description: 'Flerrads textinmatning' },
    { type: 'SELECT', label: 'Dropdown', icon: 'arrow_drop_down_circle', description: 'Välj från lista' },
    { type: 'CHECKBOX', label: 'Kryssruta', icon: 'check_box', description: 'Ja/Nej val' },
    { type: 'DATE', label: 'Datumväljare', icon: 'calendar_today', description: 'Välj datum' }
];

// Predefined fields for Swedish scout organizations
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
        type: 'PERSONAL_NUMBER',
        label: 'Personnummer',
        icon: 'badge',
        fieldType: 'TEXT',
        required: true,
        placeholder: 'ÅÅÅÅMMDD-XXXX',
        validationPattern: '^(19|20)\\d{6}-\\d{4}$',
        maxLength: 13,
        description: 'Svenskt personnummer med formatvalidering'
    },
    {
        type: 'PHONE',
        label: 'Mobilnummer',
        icon: 'phone',
        fieldType: 'PHONE',
        required: false,
        placeholder: '07X-XXX XX XX',
        validationPattern: '^07[0-9]-?\\d{3}\\s?\\d{2}\\s?\\d{2}$',
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
        description: 'E-postadress med verifiering'
    },
    {
        type: 'BIRTH_DATE',
        label: 'Födelsedatum',
        icon: 'cake',
        fieldType: 'DATE',
        required: true,
        description: 'Deltagarens födelsedatum'
    },
    {
        type: 'GUARDIAN',
        label: 'Målsman',
        icon: 'supervisor_account',
        fieldType: 'TEXT',
        required: false,
        placeholder: 'Målsmans namn och telefon',
        description: 'Visas automatiskt för deltagare under 18 år'
    },
    {
        type: 'ALLERGY',
        label: 'Matallergi / Specialkost',
        icon: 'restaurant',
        fieldType: 'TEXTAREA',
        required: false,
        placeholder: 'Beskriv eventuella allergier eller specialkost...',
        description: 'Villkorsstyrt fält för allergier'
    },
    {
        type: 'TROOP',
        label: 'Kår',
        icon: 'groups',
        fieldType: 'SELECT',
        required: false,
        description: 'Välj scoutkår från lista',
        options: []
    }
];