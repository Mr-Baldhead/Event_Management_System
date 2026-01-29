import { HttpInterceptorFn } from '@angular/common/http';

// Auth interceptor - with cookie-based auth, we just need to ensure credentials are sent
export const authInterceptor: HttpInterceptorFn = (req, next) => {
    // Clone request to add withCredentials for cookie-based auth
    const authReq = req.clone({
        withCredentials: true
    });

    return next(authReq);
};