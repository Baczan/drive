import {NgModule} from '@angular/core';
import {BrowserModule, HammerModule} from '@angular/platform-browser';

import {AppComponent} from './app.component';
import {RouterModule, Routes} from "@angular/router";
import {AuthModule} from "../modules/auth/auth.module";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {LoggedInGuard} from "../modules/auth/guards/logged-in.guard";
import {CsrfInterceptorService} from "../modules/auth/interceptors/csrf-interceptor.service";
import {PaymentComponent} from './components/payment/payment.component';
import {NgxStripeModule} from "ngx-stripe";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {SubscriptionComponent} from './components/subscription/subscription.component';
import {SubscriptionCardComponent} from './components/subscription/subscription-card/subscription-card.component';
import {MatSelectModule} from "@angular/material/select";
import {PlanDetailsCardComponent} from './components/subscription/plan-details-card/plan-details-card.component';
import {MatRadioModule} from "@angular/material/radio";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatIconModule} from "@angular/material/icon";
import { LinkComponent } from './components/minor/link/link.component';
import {MatDividerModule} from "@angular/material/divider";
import {ErrorDialog, NewSubscriptionComponent} from './components/new-subscription/new-subscription.component';
import { WrapperComponent } from './components/minor/wrapper/wrapper.component';
import { PlanCardComponent } from './components/new-subscription/plan-card/plan-card.component';
import {MatDialogModule} from "@angular/material/dialog";
import { SubscriptionDetailsComponent } from './components/new-subscription/subscription-details/subscription-details.component';
import {SubscriptionGuard} from "./guards/subscription.guard";
import {DatePipe} from "@angular/common";
import {FileComponent, FolderCreationDialogComponent} from './components/file/file.component';
import {MatMenuModule} from "@angular/material/menu";
import { FileListHeaderComponent } from './components/file/file-list-header/file-list-header.component';
import { FileListItemFolderComponent } from './components/file/file-list-item-folder/file-list-item-folder.component';
import 'hammerjs';
import { FileListItemFileComponent } from './components/file/file-list-item-file/file-list-item-file.component';
import { IconPipe } from './pipes/icon.pipe';
import {NgxFilesizeModule} from "ngx-filesize";
import { FileTransferSheetComponent } from './components/file/file-transfer-sheet/file-transfer-sheet.component';
import {MatBottomSheetModule} from "@angular/material/bottom-sheet";
import { FileUploadingItemComponent } from './components/file/file-transfer-sheet/file-uploading-item/file-uploading-item.component';
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatBadgeModule} from "@angular/material/badge";
import { FileZipItemComponent } from './components/file/file-transfer-sheet/file-zip-item/file-zip-item.component';
import {DragulaModule} from "ng2-dragula";
import {NgxDropzoneModule} from "ngx-dropzone";
import { DropzoneDirective } from './directives/dropzone.directive';
import {StorageSpaceGuard} from "./guards/storage-space.guard";
import {A11yModule} from "@angular/cdk/a11y";
import { AutofocusDirective } from './directives/autofocus.directive';
import { FolderNameChangeComponent } from './components/file/dialogs/folder-name-change/folder-name-change.component';
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatTooltipModule} from "@angular/material/tooltip";
import { GalleryComponent } from './components/gallery/gallery.component';
import { MenuComponent } from './components/menu/menu.component';
import { MenuItemComponent } from './components/menu/menu-item/menu-item.component';
import { TransferDialogComponent } from './components/file/transfer-dialog/transfer-dialog.component';


const routes: Routes = [

  {path: "payment", component: PaymentComponent, canActivate: [LoggedInGuard,SubscriptionGuard,StorageSpaceGuard]},
  {path: "files", component: FileComponent, canActivate: [LoggedInGuard,SubscriptionGuard,StorageSpaceGuard]},
  {path: "files/:id", component: FileComponent, canActivate: [LoggedInGuard,SubscriptionGuard,StorageSpaceGuard]},
  {path: "subscription", component: NewSubscriptionComponent, canActivate: [LoggedInGuard,SubscriptionGuard,StorageSpaceGuard]},
  {path: "subscription2", component: SubscriptionComponent, canActivate: [LoggedInGuard,SubscriptionGuard,StorageSpaceGuard]},
  {path: '**', redirectTo: "files",}
];


@NgModule({
  declarations: [
    AppComponent,
    PaymentComponent,
    SubscriptionComponent,
    SubscriptionCardComponent,
    PlanDetailsCardComponent,
    LinkComponent,
    NewSubscriptionComponent,
    WrapperComponent,
    PlanCardComponent,
    ErrorDialog,
    SubscriptionDetailsComponent,
    FileComponent,
    FolderCreationDialogComponent,
    FileListHeaderComponent,
    FileListItemFolderComponent,
    FileListItemFileComponent,
    IconPipe,
    FileTransferSheetComponent,
    FileUploadingItemComponent,
    FileZipItemComponent,
    DropzoneDirective,
    AutofocusDirective,
    FolderNameChangeComponent,
    GalleryComponent,
    MenuComponent,
    MenuItemComponent,
    TransferDialogComponent
  ],
  imports: [
    BrowserModule,
    AuthModule,
    HttpClientModule,
    RouterModule.forRoot(routes, {enableTracing: false}),
    RouterModule,
    NgxStripeModule.forRoot('pk_test_51JROoKBZFHL9NjRlbLa9kDHnGNiIK8HX2bnxWVR2UTkFy6nLkHjtK3PGho4fTGxQvSjl9UVIZsS8bE4xtR3fpf5x00Zc5dIHdY'),
    ReactiveFormsModule,
    BrowserAnimationsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatRadioModule,
    MatProgressSpinnerModule,
    MatSidenavModule,
    MatIconModule,
    MatDividerModule,
    MatDialogModule,
    MatMenuModule,
    FormsModule,
    HammerModule,
    NgxFilesizeModule,
    MatBottomSheetModule,
    MatProgressBarModule,
    MatBadgeModule,
    DragulaModule.forRoot(),
    NgxDropzoneModule,
    A11yModule,
    MatSnackBarModule,
    MatTooltipModule
  ],
  providers: [DatePipe,LoggedInGuard, {provide: HTTP_INTERCEPTORS, useClass: CsrfInterceptorService, multi: true},SubscriptionGuard,StorageSpaceGuard],
  bootstrap: [AppComponent]
})
export class AppModule {
}
