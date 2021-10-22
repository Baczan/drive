export class SubscriptionModel {
  public id:number;
  public userEmail:string;
  public subscriptionId:string;
  public customerId:string;
  public tier:string;
  public defaultPaymentMethod:string;
  public cancelAtPeriodEnd:boolean;
  public periodEnd:number;
  public total:number;


  constructor() {
  }
}
