import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-wrapper',
  templateUrl: './wrapper.component.html',
  styleUrls: ['./wrapper.component.css']
})
export class WrapperComponent implements OnInit {

  @Input() maxWidth = "100%";
  @Input() width = "100%";
  @Input() title: any;

  constructor() { }

  ngOnInit(): void {
  }

}
