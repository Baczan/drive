import {Component, Inject, LOCALE_ID, OnInit} from '@angular/core';
import {PaymentService} from "../../../services/payment.service";
import {AuthService} from "../../../../modules/auth/services/auth.service";
import {DatePipe, formatDate} from "@angular/common";
import {finalize} from "rxjs/operators";

@Component({
  selector: 'app-subscription-details',
  templateUrl: './subscription-details.component.html',
  styleUrls: ['./subscription-details.component.css']
})
export class SubscriptionDetailsComponent implements OnInit {

  changingState: boolean = false;
  selectingTier: boolean = false;
  changingTier: boolean = false;

  constructor(public paymentService: PaymentService, public authService: AuthService, private datePipe: DatePipe, @Inject(LOCALE_ID) private locale: string) {
  }

  ngOnInit(): void {
  }

  getDate() {

    let date: Date = new Date(this.paymentService.subscription.periodEnd * 1000);

    return formatDate(date, "MMM d, y", this.locale);
  }


  getDefaultPaymentInfoIndex() {
    let index = this.paymentService.cards.findIndex(card => {
      return card.paymentMethodId == this.paymentService.subscription.defaultPaymentMethod
    });
    return index;
  }

  changeSubscriptionState(state: boolean) {

    this.changingState = true;

    this.paymentService.updateSubscriptionState(state)
      .pipe(finalize(() => {
        this.changingState = false;
      }))
      .subscribe(response => {

      }, error => {
        window.location.reload()
      })

  }

  chooseTier(tierName: string) {

    this.changingTier = true;

    this.paymentService.changeTier(tierName)
      .pipe(finalize(() => {
        this.selectingTier = false;
        this.changingTier = false;
      }))
      .subscribe(response => {

      }, error => {
        window.location.reload()
      })

  }

}
