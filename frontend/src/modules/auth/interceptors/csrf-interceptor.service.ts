import {Injectable} from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse
} from "@angular/common/http";
import {Observable, throwError} from "rxjs";
import {AuthService} from "../services/auth.service";
import {catchError, tap} from "rxjs/operators";
import {environment} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class CsrfInterceptorService implements HttpInterceptor {

  constructor(private auth: AuthService) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    let previousEmail = this.auth.user.email;
    let previousAuthorities = this.auth.user.authorities;

    this.auth.getDataFromCookies();

    if(previousEmail!=this.auth.user.email){
      window.location.href = environment.REDIRECT_URL
    }

    if(previousAuthorities.length != this.auth.user.authorities.length){
      window.location.href = environment.REDIRECT_URL
    }

    for (let i = 0;i<previousAuthorities.length;i++){
      if(previousAuthorities[i]!=this.auth.user.authorities[i]){
        window.location.href = environment.REDIRECT_URL
      }
    }

    req = req.clone({
      withCredentials: true,
      headers:req.headers.append("X-CSRF-TOKEN",this.auth.csrfToken)
    });



    return next.handle(req).pipe(tap(response=>{


      if(response instanceof HttpResponse){

        if(response.url==`${environment.AUTHORIZATION_SERVER_URL}/login`){
          this.auth.login(window.location.href);
        }

      }

    }),catchError(err => {


      if (err instanceof HttpErrorResponse){

        if(err.url==`${environment.AUTHORIZATION_SERVER_URL}/login`){
          this.auth.login(window.location.href);
        }

        if(err.status==401){
          this.auth.login(window.location.href);
        }

        if(err.status==403){
          window.location.href = environment.REDIRECT_URL
        }

      }

      return throwError(err);
    }));
  }
}
