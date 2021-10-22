import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {SubscriptionModel} from "../models/SubscriptionModel";
import {Card} from "../models/Card";
import {SubscriptionCreation} from "../models/SubscriptionCreation";
import {tap} from "rxjs/operators";


@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  subscription:SubscriptionModel;

  cards:Card[] = [];

  constructor(private http:HttpClient) { }

  createSubscription(tier:string){
    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/stripe/createSubscription?tierName=${tier}`;
    return this.http.get<SubscriptionCreation>(url);
  }

  getCurrentSubscription(){

    let  url = `${environment.AUTHORIZATION_SERVER_URL}/api/stripe/getCurrentSubscription`;

    return this.http.get<SubscriptionModel>(url).pipe(tap(value=>{
      this.subscription = value;
    }));
  }

  updateSubscription(subscriptionId:string,paymentMethod:string){

    let  url = `${environment.AUTHORIZATION_SERVER_URL}/api/stripe/updateSubscription?subscriptionId=${subscriptionId}&paymentMethodId=${paymentMethod}`;

    return this.http.post<SubscriptionModel>(url,null).pipe(tap(value=>{
      this.subscription = value;
    }));
  }

  deleteCurrentSubscription(){
    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/stripe/deleteSubscription`;
    return this.http.post(url,null,{responseType:"text"});
  }

  getCustomerPaymentMethods(){
    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/stripe/paymentMethods`;

    return this.http.get<Card[]>(url).pipe(tap(value=>{
      this.cards = value;
    }));
  }

  updateSubscriptionState(state:boolean){

    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/stripe/subscriptionState?state=${state}`;

    return this.http.post<SubscriptionModel>(url,null).pipe(tap(value=>{
      this.subscription = value;
    }));
  }

  changeTier(tierName:string){
    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/stripe/subscriptionChangeTier?tierName=${tierName}`;

    return this.http.post<SubscriptionModel>(url,null).pipe(tap(value=>{
      this.subscription = value;
    }));
  }




}
