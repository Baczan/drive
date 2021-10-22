import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {AuthService} from "../../modules/auth/services/auth.service";

class RequestOptions {
  constructor(param: { headers: any; withCredentials: boolean }) {

  }

}

@Injectable({
  providedIn: 'root'
})
export class TestService {

  constructor(private http:HttpClient,private auth:AuthService) { }


  test1(){
    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/test/test1`;

    return this.http.get(url,{responseType:'text'});
  }

  test2(){
    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/test/test2`;

    return this.http.get(url,{responseType:'text'});
  }

  test3(){
    const url = `${environment.AUTHORIZATION_SERVER_URL}/api/test/test3`;

    let formData = new FormData();
    formData.append("fas","Fas");
    return this.http.post(url,formData,{responseType:'text'});
  }
}
