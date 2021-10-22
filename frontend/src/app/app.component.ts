import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import {environment} from "../environments/environment";
import {MatDrawer, MatDrawerContainer, MatDrawerMode} from "@angular/material/sidenav";
import {WidthService} from "./services/width.service";
import {Width} from "./models/Width";
import {Subscription} from "rxjs";
import {BreakpointObserver, BreakpointState} from "@angular/cdk/layout";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements AfterViewInit,OnDestroy,OnInit{

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.updateWidth();
    this.updateToWidth(this.widthService.width);
  }

  @ViewChild("drawerWrapper") drawerWrapper:ElementRef;
  @ViewChild("innerDrawerWrapper") innerDrawerWrapper:ElementRef;
  @ViewChild("drawer") drawer:MatDrawer;

  hasBackdrop:boolean = true;
  mode:MatDrawerMode = 'over';
  opened:boolean = false;
  previousWidth:Width;


  widthSubscription:Subscription;
  breakPointSubscription:Subscription;

  title = 'Dysk';

  constructor(private widthService:WidthService,private cdr:ChangeDetectorRef, private breakpointObserver: BreakpointObserver){
  }


  ngOnInit(): void {
    this.breakPointSubscription = this.breakpointObserver.observe('(max-width: 1000px)').subscribe((state: BreakpointState) => {

      this.opened = !state.matches;


      this.mode = state.matches ? 'over' : 'side';
      this.hasBackdrop = state.matches;

      this.cdr.detectChanges();
    });
  }

  ngAfterViewInit(): void {
    this.updateWidth();
    this.updateToWidth(this.widthService.width);
  }

  updateWidth(){
    this.widthService.width = new Width(this.drawerWrapper.nativeElement.offsetWidth,this.innerDrawerWrapper.nativeElement.offsetWidth);
    this.widthService.widthEvent.emit(new Width(this.drawerWrapper.nativeElement.offsetWidth,this.innerDrawerWrapper.nativeElement.offsetWidth))
  }


  updateToWidth(width:Width){
    if(width){

      if(width.fullWidth<=1000){
        this.hasBackdrop = true;
        this.mode="over";

        if(this.previousWidth && this.previousWidth.fullWidth>1000){
          this.drawer.close();
        }



      }else{
        this.hasBackdrop = false;
        this.mode="side";


        if(this.previousWidth && this.previousWidth.fullWidth<1000){
          this.drawer.open();
        }
      }

      this.previousWidth = width;

      this.cdr.detectChanges();
    }
  }

  isDifferentFromPreviousWidth(width:Width):boolean{
    if(this.previousWidth){

      if(this.previousWidth.fullWidth!=width.fullWidth){
        return true;
      }

      if(this.previousWidth.containerWidth!=width.containerWidth){
        return true;
      }

      return false;
    }

    return true;
  }

  ngOnDestroy(): void {
    this.widthSubscription.unsubscribe();
    this.breakPointSubscription.unsubscribe();
  }


}
