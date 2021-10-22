import {Component, OnInit, ViewChild} from '@angular/core';
import {PaymentService} from "../../services/payment.service";
import {SubscriptionModel} from "../../models/SubscriptionModel";
import {finalize} from "rxjs/operators";
import {StripeCardElementOptions, StripeCardNumberElementOptions, StripeElementStyle} from "@stripe/stripe-js";
import {StripeCardComponent, StripeCardNumberComponent, StripeService} from "ngx-stripe";
import {FormControl, Validators} from "@angular/forms";
import {Card} from "../../models/Card";

@Component({
  selector: 'app-subscription',
  templateUrl: './subscription.component.html',
  styleUrls: ['./subscription.component.css']
})
export class SubscriptionComponent {

/*  loading = true;
  subscriptionModel: SubscriptionModel;
  nameControl: FormControl = new FormControl("", Validators.required);
  cards: Card[] = [];
  radioFromControl: FormControl = new FormControl("");

  loadingCards: boolean = false;
  deletingSubscription: boolean = false;
  creatingSubscription: boolean = false;
  paying: boolean = false;

  @ViewChild(StripeCardNumberComponent) card: StripeCardNumberComponent;

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

  constructor(private paymentService: PaymentService, private stripeService: StripeService) {
  }

  ngOnInit(): void {

    this.paymentService.getCurrentSubscription()
      .pipe(finalize(() => {
        this.loading = false;
      }))
      .subscribe(response => {

        this.subscriptionModel = response;

        if (this.subscriptionModel) {
          this.getCustomer();
        }

      }, error => {
        console.log(error);
      })

  }


  createSubscription(tier: string) {
    this.creatingSubscription = true;
    this.paymentService.subscription(tier)
      .pipe(finalize(() => {
        this.creatingSubscription = false;
      }))
      .subscribe(response => {

        this.subscriptionModel = response;

      }, error => {
        console.log(error)
      });

    this.getCustomer();
  }

  pay() {

    let paymentMethod;
    if (this.cards.length > 0) {

      if (this.radioFromControl.value == "newCard") {
        paymentMethod = {
          card: this.card.element,
          billing_details: {
            name: this.nameControl.value
          }
        };
      } else {
        paymentMethod = this.cards[this.radioFromControl.value].paymentMethodId;
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
    this.stripeService.confirmCardPayment(this.subscriptionModel.clientSecret, {
      payment_method: paymentMethod
    })
      .pipe(finalize(() => {
        this.paying = false;
      }))
      .subscribe(response => {
        console.log(response)
      }, error => {
        console.log(error)
      })

  }


  deleteSubscription() {
    this.deletingSubscription = true;
    this.paymentService.deleteCurrentSubscription()
      .pipe(finalize(() => {
        this.deletingSubscription = false;
      }))
      .subscribe(response => {
        // @ts-ignore
        this.subscriptionModel = null;
      }, error => {
        console.log(error)
      })
  }

  getCustomer() {

    this.loadingCards = true;
    this.paymentService.getCustomerPaymentMethods()
      .pipe(finalize(() => {
        this.loadingCards = false;
      }))
      .subscribe(response => {
        this.cards = response;
        if (this.cards.length > 0) {
          this.radioFromControl.setValue(0);
        }
      }, error => {
        console.log(error)
      })
  }

  showSpinner() {

    return this.loadingCards || this.deletingSubscription || this.creatingSubscription;

  }*/

}
