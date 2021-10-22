import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'icon'
})
export class IconPipe implements PipeTransform {

  icons = {
    ".jpg": "photo",
    ".txt":"article"
  };

  transform(value: string, ...args: unknown[]): string {

    let lastDot = value.lastIndexOf(".");

    if (lastDot < 0) {
      return "description";
    }

    let extension = value.substring(lastDot);


    // @ts-ignore
    if (this.icons[extension]) {
      // @ts-ignore
      return this.icons[extension];
    }


    return "description";
  }

}



