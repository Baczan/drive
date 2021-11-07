import {Folder} from "./Folder";
import {TransferFolder} from "./TransferFolder";
import {FileEntity} from "./FileEntity";

export class TransferFolderResponse{
  public folder:Folder;
  public transferFolders:TransferFolder[];
  public files:FileEntity[];
}
