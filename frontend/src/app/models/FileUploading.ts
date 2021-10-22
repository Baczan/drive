import {Subscription} from "rxjs";

export class FileUploading {
  public filename:string;
  public subscription:Subscription;
  public progress:number= 0;
  public error:boolean = false;
  public completed:boolean = false;
  public seen:boolean = false;


  constructor() {
  }

}
