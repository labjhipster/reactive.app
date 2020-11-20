import { Moment } from 'moment';

export interface IPost {
  id?: string;
  title?: string;
  content?: any;
  date?: Moment;
}

export class Post implements IPost {
  constructor(public id?: string, public title?: string, public content?: any, public date?: Moment) {}
}
