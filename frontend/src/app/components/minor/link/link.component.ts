import {ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {AuthService} from "../../../../modules/auth/services/auth.service";
import {MatDrawer} from "@angular/material/sidenav";

@Component({
  selector: 'app-link',
  templateUrl: './link.component.html',
  styleUrls: ['./link.component.css']
})
export class LinkComponent implements OnInit {

  @Input() icon:string;
  @Input() text:string;
  @Input() path:string;
  @Input() disableHighlight:boolean = false;
  @Input() drawer:MatDrawer;

  @Input() logout:boolean = false;


  constructor(private auth:AuthService,private cdr:ChangeDetectorRef) { }

  ngOnInit(): void {
  }

  handleClick(){
    if(this.logout){
      this.auth.logout();
    }

    if(this.drawer.mode=="over"){

      this.drawer.close();

    }


  }


}
