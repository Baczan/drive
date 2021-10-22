import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef, EventEmitter,
  Input,
  OnDestroy,
  OnInit, Output,
  ViewChild
} from '@angular/core';
import {WidthService} from "../../../services/width.service";
import {Subscription} from "rxjs";
import {Width} from "../../../models/Width";

@Component({
  selector: 'app-plan-card',
  templateUrl: './plan-card.component.html',
  styleUrls: ['./plan-card.component.css']
})
export class PlanCardComponent implements OnInit, OnDestroy, AfterViewInit {


  @Output() tierChosen:EventEmitter<any> = new EventEmitter<any>();

  constructor(private widthService: WidthService, private cdr: ChangeDetectorRef) {
  }

  @Input() title: string;
  @Input() size: number;
  @Input() price: string;
  @Input() current: boolean;

  @ViewChild("card") card: ElementRef;

  width = 25;
  cardWidth = 250;
  widthSubscription: Subscription;

  ngOnInit(): void {

    this.widthSubscription = this.widthService.widthEvent.subscribe(width => {
      this.adjust(width)
    })

  }

  adjust(width: Width) {
    this.cardWidth = this.card.nativeElement.offsetWidth;

    if (width.containerWidth <= 550) {
      this.width = 100;
    } else if (width.containerWidth <= 800) {
      this.width = 50;
    } else if (width.containerWidth <= 1000) {
      this.width = 33;
    } else {
      this.width = 25;
    }

    this.cdr.detectChanges();
  }

  getWidth() {
    return this.width + "%";
  }

  ngOnDestroy(): void {
    this.widthSubscription.unsubscribe();
  }


  getTitleFontSize() {
    if (this.card) {
      return this.card.nativeElement.offsetWidth / 11 + "px"
    }
    return "15px"
  }

  getSizeFontSize() {
    if (this.card) {
      return this.card.nativeElement.offsetWidth / 10 + "px"
    }
    return "15px"
  }

  getPriceFontSize() {
    if (this.card) {
      return this.card.nativeElement.offsetWidth / 13 + "px"
    }
    return "15px"
  }

  getButtonFontSize() {
    if (this.card) {
      return this.card.nativeElement.offsetWidth / 15 + "px"
    }
    return "15px"
  }

  getCurrentFontSize() {
    if (this.card) {
      return this.card.nativeElement.offsetWidth / 14 + "px"
    }
    return "15px"
  }

  ngAfterViewInit(): void {
    this.adjust(this.widthService.width)
  }


}
