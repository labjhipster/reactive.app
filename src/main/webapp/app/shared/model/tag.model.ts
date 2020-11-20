export interface ITag {
  id?: string;
  name?: string;
}

export class Tag implements ITag {
  constructor(public id?: string, public name?: string) {}
}
