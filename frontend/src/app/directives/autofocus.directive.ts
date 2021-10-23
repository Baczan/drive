import {AfterViewInit, Directive, ElementRef, Input} from '@angular/core';

@Directive({
  selector: '[appAutofocus]'
})
export class AutofocusDirective implements AfterViewInit{


  constructor(public element: ElementRef<HTMLElement>) {
  }

  ngAfterViewInit(): void {
      setTimeout(() => this.element.nativeElement.focus(), 0);
  }

}
