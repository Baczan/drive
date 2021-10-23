export class Folder {
  public id:string;
  public user:string;
  public folderName:string;
  public ancestry:string | null;
  public parentId: string | null;
  public favorite:boolean = false;

  constructor() {
  }
}
