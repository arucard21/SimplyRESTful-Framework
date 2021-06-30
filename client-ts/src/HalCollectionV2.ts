import { HALResource } from './HALResource';
import { HalLink } from 'hal-types';

export type HalCollectionV2<T extends HALResource> = {
    total: number;
    _links: {
        first: HalLink,
        last: HalLink,
        prev: HalLink,
        next: HalLink;
    };
    _embedded: {
        item: T[];
    };
} & T;
