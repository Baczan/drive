import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-subscription-card',
  templateUrl: './subscription-card.component.html',
  styleUrls: ['./subscription-card.component.css']
})
export class SubscriptionCardComponent implements OnInit {

  @Input() name:string;
  @Input() price:string;
  @Input() size:string;
  @Input() current:boolean = false;

  @Output() clicked:EventEmitter<any> = new EventEmitter();

  constructor() { }

  ngOnInit(): void {
  }

}
