import { Moment } from 'moment';
import { IBlog } from 'app/shared/model/blog.model';
import { ITag } from 'app/shared/model/tag.model';

export interface IPost {
  id?: string;
  title?: string;
  content?: any;
  date?: Moment;
  blog?: IBlog;
  tags?: ITag[];
}

export class Post implements IPost {
  constructor(
    public id?: string,
    public title?: string,
    public content?: any,
    public date?: Moment,
    public blog?: IBlog,
    public tags?: ITag[]
  ) {}
}
