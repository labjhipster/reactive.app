export interface IBlog {
  id?: string;
  name?: string;
  handle?: string;
}

export class Blog implements IBlog {
  constructor(public id?: string, public name?: string, public handle?: string) {}
}
