import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot, UrlTree } from '@angular/router';
import {Observable, throwError} from 'rxjs';
import {PaymentService} from "../services/payment.service";
import {catchError, map} from "rxjs/operators";
import {AuthService} from "../../modules/auth/services/auth.service";

@Injectable({
  providedIn: 'root'
})
export class SubscriptionGuard implements CanActivate {

  constructor(private paymentService:PaymentService,private authService:AuthService){

  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {


    return this.paymentService.getCurrentSubscription().pipe(map(()=>{
      return true;
    }),catchError(err => {
      this.authService.logout();
      return throwError(err);
    }));

  }

}
