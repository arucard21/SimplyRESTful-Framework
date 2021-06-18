import { HalResource } from 'hal-types';

export class ExampleResource implements HalResource<string, any> {
    description : string;
    complexAttribute : {
        name : string;
    };
}