import {Component, OnInit, ViewChild} from '@angular/core';
import {PaymentService} from "../../services/payment.service";
import {StripeCardComponent, StripeService} from "ngx-stripe";
import {
  StripeCardCvcElementOptions,
  StripeCardElementOptions, StripeCardExpiryElementOptions,
  StripeCardNumberElementOptions,
  StripeElementStyle,
  StripeP24BankElement
} from "@stripe/stripe-js";
import {FormControl, FormGroup, Validators} from "@angular/forms";


class StripeElementsOptions {
}

@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css']
})
export class PaymentComponent implements OnInit {

  // @ts-ignore
  @ViewChild(StripeCardComponent) card: StripeCardComponent;

  style:StripeElementStyle = {
      base: {
        fontSize: '18px',
      }
  };


  numberOptions:StripeCardNumberElementOptions = {
    showIcon:true,
    style:this.style,
  };



  defaultOptions: StripeCardElementOptions = {
    style:this.style
  };



  constructor(private paymentService: PaymentService, private stripeService: StripeService) {
  }

  ngOnInit(): void {
  }


  pay() {

    this.stripeService.confirmCardPayment("pi_3JV3hoBZFHL9NjRl1scA10am_secret_aYyIDidJmsQlDAHFsxBMdTPUe", {
      payment_method: {
        card: this.card.element,
        billing_details: {
          name: "Wiktor Bachan"
        }
      }
    }).subscribe(response => {
      console.log(response)
    }, error => {
      console.log(error)
    })

  }



}
