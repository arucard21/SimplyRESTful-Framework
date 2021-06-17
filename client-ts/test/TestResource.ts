import { HalResource } from 'hal-types';

export class TestResource implements HalResource<string, any> {
    additionalField : string;
}