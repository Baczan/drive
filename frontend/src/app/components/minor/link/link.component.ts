import {Component, Input, OnInit} from '@angular/core';
import {AuthService} from "../../../../modules/auth/services/auth.service";

@Component({
  selector: 'app-link',
  templateUrl: './link.component.html',
  styleUrls: ['./link.component.css']
})
export class LinkComponent implements OnInit {

  @Input() icon:string;
  @Input() text:string;
  @Input() path:string;

  @Input() logout:boolean = false;


  constructor(private auth:AuthService) { }

  ngOnInit(): void {
  }

  handleClick(){
    if(this.logout){
      this.auth.logout();
    }
  }

}
