import { Injectable } from "@angular/core";
import { JwtHelperService } from "@auth0/angular-jwt";
import { HttpClient, HttpHeaders } from "@angular/common/http";
// import { CookieService } from "ngx-cookie-service";

import { TOKEN_NAME } from "../shared/auth.constant";

@Injectable({
  providedIn: "root"
})
export class UserService {
  jwtHelper: JwtHelperService = new JwtHelperService();
  accessToken: string;
  isAdmin: boolean;

  constructor() {}

  login(accessToken: string) {
    const decodedToken = this.jwtHelper.decodeToken(accessToken);
    // console.log(decodedToken);

    this.isAdmin = decodedToken.authorities.some(el => el === "ADMIN_USER");
    this.accessToken = accessToken;

    localStorage.setItem(TOKEN_NAME, accessToken);
    // this.cookieService.set(TOKEN_NAME, accessToken);
  }

  logout() {
    this.accessToken = null;
    this.isAdmin = false;
    localStorage.removeItem(TOKEN_NAME);
    // this.cookieService.delete(TOKEN_NAME);
  }

  isAdminUser(): boolean {
    return this.isAdmin;
  }

  isUser(): boolean {
    return this.accessToken && !this.isAdmin;
    // return this.cookieService.get(TOKEN_NAME) && !this.isAdmin;
  }
}
