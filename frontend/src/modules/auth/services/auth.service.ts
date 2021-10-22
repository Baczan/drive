import { Injectable } from '@angular/core';
import * as crypto from 'crypto-js';
import {environment} from "../../../environments/environment";
import {ActivatedRoute, Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {CookieService} from "ngx-cookie-service";
import {User} from "../models/User";


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  user:User= new User();


  // @ts-ignore
  csrfToken:string = null;

  constructor(private router:Router,private http:HttpClient,private cookieService:CookieService) { }


  login(redirectUrl:string = environment.REDIRECT_URL) {
    window.location.href = `${environment.AUTHORIZATION_SERVER_URL}/login?redirectUrl=${redirectUrl}`;
  }


  getDataFromCookies(){
    this.csrfToken = this.cookieService.get("csrf");
    this.user.email = this.cookieService.get("email");
    if(this.cookieService.get("authorities")){

      let authoritiesString = atob(this.cookieService.get("authorities"));
      authoritiesString = authoritiesString.substring(1,(authoritiesString.length-1));
      this.user.authorities = authoritiesString.split(",");
    }

  }

  isLoggedIn():boolean{
    return !!this.user.email && !!this.csrfToken;
  }

  logout(){
    window.location.href = `${environment.AUTHORIZATION_SERVER_URL}/logout`;
  }





}
