import {Injectable} from '@angular/core';
import {WebSocketSubject} from "rxjs/internal-compatibility";
import {catchError, switchAll, tap} from "rxjs/operators";
import {EMPTY, Subject} from "rxjs";
import {webSocket} from "rxjs/webSocket";
import {RxStomp} from '@stomp/rx-stomp';
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  public rxStomp:RxStomp;

  constructor(){

    this.rxStomp = new RxStomp();

    this.rxStomp.configure({
      brokerURL:environment.WEBSOCKET_URL,
      heartbeatIncoming:0,
      heartbeatOutgoing:20000,
      reconnectDelay:200,
      debug:(msg:string):void=>{
        console.log(new Date(),msg)
      }
    });

    this.rxStomp.activate();

  }

}

