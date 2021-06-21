import { HalResource } from 'hal-types';

type TestResource<T extends HalResource<string, unknown> = HalResource<string, unknown>> = {
    additionalField : string;
} & T;