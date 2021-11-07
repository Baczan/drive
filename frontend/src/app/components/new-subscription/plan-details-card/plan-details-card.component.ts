import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-plan-details-card',
  templateUrl: './plan-details-card.component.html',
  styleUrls: ['./plan-details-card.component.css']
})
export class PlanDetailsCardComponent implements OnInit {

  @Input() name:string;
  @Input() price:string;
  @Input() size:string;

  constructor() { }

  ngOnInit(): void {
  }

}
