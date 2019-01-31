import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {PropertyCreatorComponent} from "./property-creator.component";
import {FormsModule} from "@angular/forms";
import {FormElementsModule} from "app/ng2/components/ui/form-components/form-elements.module";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";
import {TranslateModule} from "../../../shared/translator/translate.module";

@NgModule({
    declarations: [
        PropertyCreatorComponent,
    ],
    imports: [
        CommonModule,
        FormsModule,
        FormElementsModule,
        UiElementsModule,
        TranslateModule
    ],
    exports: [],
    entryComponents: [
        PropertyCreatorComponent
    ],
    providers: []
})

export class PropertyCreatorModule {}
