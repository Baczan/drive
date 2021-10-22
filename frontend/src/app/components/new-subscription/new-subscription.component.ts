import {AfterViewInit, ChangeDetectorRef, Component, Inject, OnInit, ViewChild} from '@angular/core';
import {PaymentService} from "../../services/payment.service";
import {delay, finalize} from "rxjs/operators";
import {SubscriptionModel} from "../../models/SubscriptionModel";
import {StripeCardNumberComponent, StripeService} from "ngx-stripe";
import {
  StripeError,
  PaymentIntent,
  StripeCardElementOptions,
  StripeCardNumberElementOptions,
  StripeElementStyle
} from "@stripe/stripe-js";
import {FormControl, Validators} from "@angular/forms";
import {Card} from "../../models/Card";
import {HttpErrorResponse} from "@angular/common/http";
import {MAT_DIALOG_DATA, MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-new-subscription',
  templateUrl: './new-subscription.component.html',
  styleUrls: ['./new-subscription.component.css']
})
export class NewSubscriptionComponent implements OnInit {

  @ViewChild(StripeCardNumberComponent) card: StripeCardNumberComponent;

  tier: any;

  loadingSubscription: boolean = true;
  creatingSubscription: boolean = false;
  paying: boolean = false;
  loadingCards: boolean = false;
  loadingSubscriptionAfterPayment: boolean = false;
  loadingPayments:boolean = false;

  numberComplete: boolean = false;
  cvcComplete: boolean = false;
  expiryComplete: boolean = false;

  clientSecret: any;
  subscriptionId: any;
  associatedTier: any;

  nameControl: FormControl = new FormControl("", Validators.required);
  radioFromControl: FormControl = new FormControl("newCard");


  style: StripeElementStyle = {
    base: {
      fontSize: '18px',
    }
  };


  numberOptions: StripeCardNumberElementOptions = {
    showIcon: true,
    style: this.style,
  };


  defaultOptions: StripeCardElementOptions = {
    style: this.style
  };

  constructor(public paymentService: PaymentService, private stripeService: StripeService, private cdr: ChangeDetectorRef,public dialog: MatDialog) {

    paymentService.getCurrentSubscription()
      .pipe(finalize(() => {
        this.loadingSubscription = false;
      }))
      .subscribe(response => {

      }, error => {
        window.location.reload()
      });

    this.getPaymentMethods();
  }

  ngOnInit(): void {
  }


  chooseTier(tier: string) {

    if (this.associatedTier) {

      if (this.associatedTier != tier) {
        this.clientSecret = null;
        this.associatedTier = null;
      }

    }
    this.tier = tier;
  }

  changeTier() {
    this.tier = null;
    this.resetForms()
  }



  pay() {

    this.nameControl.markAllAsTouched();

    if (this.nameControl.valid || this.radioFromControl.value != "newCard") {

      if (this.clientSecret) {
        this.attemptToPay();
      } else {

        this.creatingSubscription = true;
        this.paymentService.createSubscription(this.tier)
          .pipe(finalize(() => {
            this.creatingSubscription = false;
          }))
          .subscribe(response => {
            this.clientSecret = response.clientSecret;
            this.subscriptionId = response.subscriptionId;
            this.associatedTier = this.tier;
            this.attemptToPay()
          }, (error:HttpErrorResponse) => {

            window.location.reload()
          })
      }

    }

  }

  attemptToPay() {


    let paymentMethod;
    if (this.paymentService.cards.length > 0) {

      if (this.radioFromControl.value == "newCard") {
        paymentMethod = {
          card: this.card.element,
          billing_details: {
            name: this.nameControl.value
          }
        };
      } else {
        paymentMethod = this.paymentService.cards[this.radioFromControl.value].paymentMethodId;
      }

    } else {

      paymentMethod = {
        card: this.card.element,
        billing_details: {
          name: this.nameControl.value
        }
      };

    }

    this.paying = true;
    this.stripeService.confirmCardPayment(this.clientSecret, {
      payment_method: paymentMethod
    })
      .pipe(
        finalize(() => {
          this.paying = false;
        })
      )
      .subscribe(response => {

        if (response.error) {

          if (response.error.code == 'payment_intent_unexpected_state') {
            window.location.reload();
          }else if(response.error.type!="api_error"){

            let dialogRef = this.dialog.open(ErrorDialog, {
              data: response.error.message
            });

          }



        } else {

          this.loadingSubscriptionAfterPayment = true;
          // @ts-ignore
          this.paymentService.updateSubscription(this.subscriptionId, response.paymentIntent.payment_method)
            .pipe(
              finalize(() => {
                this.loadingSubscriptionAfterPayment = false;
              })
            )
            .subscribe(response => {
              this.getPaymentMethods();
            }, error => {
              window.location.reload()
            })
        }


      }, error => {
        window.location.reload()
      });


  }

  handleChange(event: any) {

    if (event.elementType == "cardNumber") {
      this.numberComplete = event.complete;
    } else if (event.elementType == "cardExpiry") {
      this.expiryComplete = event.complete;
    } else if (event.elementType == "cardCvc") {
      this.cvcComplete = event.complete;
    }
  }


  getTitle() {
    if (!this.tier && !this.paymentService.subscription) {
      return "Wybierz plan"
    } else if (this.tier && !this.paymentService.subscription) {
      return "Dokonaj płatności"
    } else {
      return "Subskrypcja"
    }
  }

  getPaymentMethods() {

    this.loadingCards = true;
    this.loadingPayments = true;
    this.paymentService.getCustomerPaymentMethods()
      .pipe(finalize(() => {
        this.loadingCards = false;
        this.loadingPayments = false;
      }))
      .subscribe(response => {

        if (this.paymentService.cards.length > 0) {
          this.radioFromControl.setValue(0);
        }
      }, error => {
        console.log(error)
      })

  }

  resetForms() {
    this.cvcComplete = false;
    this.expiryComplete = false;
    this.numberComplete = false;
    this.nameControl.reset();

  }



  payAvailable() {
    return (
      (this.numberComplete
        && this.expiryComplete
        && this.cvcComplete
        && this.nameControl.valid) || this.radioFromControl.value != "newCard");
  }

  payLoading() {

    return !this.paying
      && !this.creatingSubscription
      && !this.loadingSubscriptionAfterPayment
      && !this.loadingCards;

  }

  showSpinner() {
    return this.loadingSubscription;
  }

}

@Component({
  selector: 'error_dialog',
  templateUrl: 'error_dialog.html',
})
export class ErrorDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public data: string) {}
}


