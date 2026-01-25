import { ApplicationConfig, LOCALE_ID } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAnimationsAsync(),
    
    // ============================================
    // IMPORTANT: Required for MatDatepicker to work!
    // ============================================
    provideNativeDateAdapter(),
    
    // Swedish locale for dates
    { provide: MAT_DATE_LOCALE, useValue: 'sv-SE' },
    { provide: LOCALE_ID, useValue: 'sv-SE' }
  ]
};
