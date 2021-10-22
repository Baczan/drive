import {EventEmitter, Injectable} from '@angular/core';
import {Width} from "../models/Width";

@Injectable({
  providedIn: 'root'
})
export class WidthService {

  visible = "flex";
  notVisible = "none";

  nameDisplay = this.visible;
  sizeDisplay = this.visible;
  dateDisplay = this.visible;

  width:Width;
  widthEvent:EventEmitter<Width> = new EventEmitter<Width>();

  constructor() {

    this.widthEvent.subscribe(width=>{
      this.adjustUI(width)
    })

  }

  adjustUI(width:Width){

    if(width.containerWidth<1000){

      this.nameDisplay = this.visible;
      this.sizeDisplay = this.visible;
      this.dateDisplay = this.notVisible;

    }else {
      this.nameDisplay = this.visible;
      this.sizeDisplay = this.visible;
      this.dateDisplay = this.visible;
    }

  }


}
