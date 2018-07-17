import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { map } from "rxjs/operators";

import {
  TOKEN_AUTH_PASSWORD,
  TOKEN_AUTH_USERNAME
} from "../shared/auth.constant";

@Injectable({
  providedIn: "root"
})
export class AuthenticationService {
  static AUTH_TOKEN = "/oauth/token";

  constructor(private http: HttpClient) {}

  login(username: string, password: string) {
    const body = `username=${encodeURIComponent(
      username
    )}&password=${encodeURIComponent(password)}&grant_type=password`;

    const httpOptions = {
      headers: new HttpHeaders({
        "Content-Type": "application/x-www-form-urlencoded",
        Authorization:
          "Basic " + btoa(TOKEN_AUTH_USERNAME + ":" + TOKEN_AUTH_PASSWORD)
      })
    };

    return this.http
      .post(AuthenticationService.AUTH_TOKEN, body, httpOptions)
      .pipe(
        map((res: any) => {
          if (res.access_token) {
            return res.access_token;
          }
          return null;
        })
      );
  }
}
