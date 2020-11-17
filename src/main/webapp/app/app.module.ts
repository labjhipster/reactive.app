import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import './vendor';
import { ReactiveAppSharedModule } from 'app/shared/shared.module';
import { ReactiveAppCoreModule } from 'app/core/core.module';
import { ReactiveAppAppRoutingModule } from './app-routing.module';
import { ReactiveAppHomeModule } from './home/home.module';
import { ReactiveAppEntityModule } from './entities/entity.module';
// jhipster-needle-angular-add-module-import JHipster will add new module here
import { MainComponent } from './layouts/main/main.component';
import { NavbarComponent } from './layouts/navbar/navbar.component';
import { FooterComponent } from './layouts/footer/footer.component';
import { PageRibbonComponent } from './layouts/profiles/page-ribbon.component';
import { ActiveMenuDirective } from './layouts/navbar/active-menu.directive';
import { ErrorComponent } from './layouts/error/error.component';

@NgModule({
  imports: [
    BrowserModule,
    ReactiveAppSharedModule,
    ReactiveAppCoreModule,
    ReactiveAppHomeModule,
    // jhipster-needle-angular-add-module JHipster will add new module here
    ReactiveAppEntityModule,
    ReactiveAppAppRoutingModule,
  ],
  declarations: [MainComponent, NavbarComponent, ErrorComponent, PageRibbonComponent, ActiveMenuDirective, FooterComponent],
  bootstrap: [MainComponent],
})
export class ReactiveAppAppModule {}
